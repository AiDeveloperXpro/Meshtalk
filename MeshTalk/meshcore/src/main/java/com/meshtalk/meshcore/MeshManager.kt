package com.meshtalk.meshcore

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MeshManager(private val context: Context, private val myId: String) {
    private val bluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val meshServiceUuid = UUID.fromString("0000b1c7-0000-1000-8000-00805f9b34fb")
    private val discoveredPeers = ConcurrentHashMap<String, BluetoothDevice>()
    private var onMessageReceived: ((from: String, message: MeshMessage) -> Unit)? = null
    private var isDiscovering = false

    // BLE scan callback
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                discoveredPeers[device.address] = device
            }
        }
    }

    // BLE advertiser
    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertisingCallback: AdvertiseCallback? = null

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (isDiscovering) return
        isDiscovering = true
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(meshServiceUuid)).build()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner?.startScan(listOf(filter), settings, scanCallback)
        advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        val data = AdvertiseData.Builder().addServiceUuid(ParcelUuid(meshServiceUuid)).build()
        val settingsAdv = AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY).build()
        advertisingCallback = object : AdvertiseCallback() {}
        advertiser?.startAdvertising(settingsAdv, data, advertisingCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        if (!isDiscovering) return
        isDiscovering = false
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        advertiser?.stopAdvertising(advertisingCallback)
    }

    // Send a message to a specific peer (direct)
    fun sendMessage(peer: BluetoothDevice, message: MeshMessage) {
        // TODO: Use GATT to send message directly
        // For now, just log
        Log.d("MeshManager", "Send to ${peer.address}: ${message.toJson()}")
    }

    // Broadcast a message to all discovered peers
    fun broadcastMessage(message: MeshMessage) {
        for (peer in discoveredPeers.values) {
            sendMessage(peer, message)
        }
    }

    // Receive a message (called by BLE/GATT callback)
    fun receiveMessage(from: String, json: String) {
        val msg = MeshMessage.fromJson(json)
        // Relay if not for me and not already seen (TODO: add deduplication)
        if (msg.to == null || msg.to == myId) {
            onMessageReceived?.invoke(from, msg)
        } else {
            // Relay
            broadcastMessage(msg)
        }
    }

    // Register callback for received messages
    fun setOnMessageReceived(callback: (from: String, message: MeshMessage) -> Unit) {
        onMessageReceived = callback
    }

    fun getPeers(): List<BluetoothDevice> = discoveredPeers.values.toList()
}