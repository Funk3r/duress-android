package com.duress.app.data

import com.duress.app.trigger.TriggerSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class TriggerLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val source: TriggerSource,
    val details: String = ""
) {
    fun formattedTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Failure-safe flat string representation: "id|timestamp|sourceId|details"
    fun serialize(): String {
        val cleanDetails = details.replace("|", "/")
        return "$id|$timestamp|${source.id}|$cleanDetails"
    }

    companion object {
        fun deserialize(raw: String): TriggerLogEntry? {
            return try {
                val parts = raw.split("|")
                if (parts.size >= 4) {
                    TriggerLogEntry(
                        id = parts[0],
                        timestamp = parts[1].toLongOrNull() ?: System.currentTimeMillis(),
                        source = TriggerSource.fromId(parts[2]),
                        details = parts.subList(3, parts.size).joinToString("|")
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}
