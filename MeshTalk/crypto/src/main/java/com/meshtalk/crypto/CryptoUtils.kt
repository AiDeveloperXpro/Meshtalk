package com.meshtalk.crypto

object CryptoUtils {
    fun generateKeyPair(): Pair<ByteArray, ByteArray> {
        // TODO: Generate public/private key pair using libsodium
        return Pair(ByteArray(32), ByteArray(32))
    }

    fun encrypt(message: ByteArray, publicKey: ByteArray, privateKey: ByteArray): ByteArray {
        // TODO: Encrypt message using libsodium
        return message
    }

    fun decrypt(cipher: ByteArray, publicKey: ByteArray, privateKey: ByteArray): ByteArray {
        // TODO: Decrypt message using libsodium
        return cipher
    }
}