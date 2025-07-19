package com.meshtalk.meshcore

import org.json.JSONObject

// Core mesh message protocol
data class MeshMessage(
    val from: String,
    val to: String?, // null for broadcast
    val timestamp: Long,
    val type: String, // e.g. "text", "file"
    val payload: String // JSON-encoded payload
) {
    fun toJson(): String = JSONObject(mapOf(
        "from" to from,
        "to" to to,
        "timestamp" to timestamp,
        "type" to type,
        "payload" to payload
    )).toString()

    companion object {
        fun fromJson(json: String): MeshMessage {
            val obj = JSONObject(json)
            return MeshMessage(
                from = obj.getString("from"),
                to = if (obj.isNull("to")) null else obj.getString("to"),
                timestamp = obj.getLong("timestamp"),
                type = obj.getString("type"),
                payload = obj.getString("payload")
            )
        }
    }
}