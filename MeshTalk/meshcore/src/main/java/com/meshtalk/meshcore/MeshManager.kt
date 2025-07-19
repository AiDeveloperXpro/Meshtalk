package com.meshtalk.meshcore

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.bluetooth.BluetoothGatt
import import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.ParcelUuid
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MeshManager(private val context: Context, private val myId: String) {
    private val bluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val meshServiceUuid = UUID.fromString("0000b1c7-0000-1000-8000-00805f9b34fb")
    private val discoveredPeers = ConcurrentHashMap<String, BluetoothDevice>()
    private var onMessageReceived: ((from: String, message: MeshMessage) -> Unit)? = null
    private var isDiscovering = false
    private var gattServer: BluetoothGattServer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val MESSAGE_CHAR_UUID = UUID.fromString("0000b1c8-0000-1000-8000-00805f9b34fb")

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

    // Start GATT server for incoming messages
    @SuppressLint("MissingPermission")
    private fun startGattServer() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = bluetoothManager.openGattServer(context, object : BluetoothGattServerCallback() {
            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice?,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic?,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
            ) {
                if (characteristic?.uuid == MESSAGE_CHAR_UUID && value != null && device != null) {
                    val json = String(value)
                    handler.post {
                        receiveMessage(device.address, json)
                    }
                }
                if (responseNeeded && gattServer != null && device != null) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            }
        })
        val service = BluetoothGattService(meshServiceUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val messageChar = BluetoothGattCharacteristic(
            MESSAGE_CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(messageChar)
        gattServer?.addService(service)
    }

    // Stop GATT server
    fun stopGattServer() {
        gattServer?.close()
        gattServer = null
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (isDiscovering) return
        isDiscovering = true
        startGattServer()
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
        stopGattServer()
    }

    // Send a message to a specific peer (direct, via GATT)
    @SuppressLint("MissingPermission")
    fun sendMessage(peer: BluetoothDevice, message: MeshMessage) {
        // Connect as GATT client and write to characteristic
        peer.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    gatt.discoverServices()
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    gatt.close()
                }
            }
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                val service = gatt.getService(meshServiceUuid)
                val char = service?.getCharacteristic(MESSAGE_CHAR_UUID)
                if (char != null) {
                    char.value = message.toJson().toByteArray()
                    gatt.writeCharacteristic(char)
                } else {
                    gatt.disconnect()
                }
            }
            override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                gatt.disconnect()
            }
        })
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