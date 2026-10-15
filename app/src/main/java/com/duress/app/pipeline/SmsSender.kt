package com.duress.app.pipeline

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.duress.app.R
import com.duress.app.data.PipelineConfig

object SmsSender {

    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Silently sends SMS message. Supports multipart messages.
     * Route depends on config.isTestMode:
     * - If test mode: sends ONLY to testPhoneNumber with "[TEST MODE] " prefix.
     * - If live: sends to emergencyContacts.
     * Failure-isolated: never throws.
     */
    fun sendSosSms(
        context: Context,
        config: PipelineConfig,
        mapsUrl: String
    ): Pair<Int, Int> { // Pair(sentCount, failedCount)
        if (!hasSmsPermission(context)) {
            return Pair(0, 0)
        }

        var sent = 0
        var failed = 0

        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java) ?: @Suppress("DEPRECATION") SmsManager.getDefault()
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val rawTemplate = if (config.customSmsTemplate.isNotBlank()) {
                config.customSmsTemplate
            } else {
                context.getString(R.string.default_sms_message, mapsUrl)
            }

            // Ensure maps link is embedded if template has placeholder
            val finalBody = if (rawTemplate.contains("%1\$s") || rawTemplate.contains("%s")) {
                try { String.format(rawTemplate, mapsUrl) } catch (_: Exception) { "$rawTemplate $mapsUrl" }
            } else if (!rawTemplate.contains("http")) {
                "$rawTemplate $mapsUrl"
            } else {
                rawTemplate
            }

            val targetNumbers = mutableListOf<String>()

            if (config.isTestMode) {
                if (config.testPhoneNumber.isNotBlank()) {
                    targetNumbers.add(config.testPhoneNumber)
                }
            } else {
                targetNumbers.addAll(config.emergencyContacts)
            }

            if (targetNumbers.isEmpty()) {
                return Pair(0, 0)
            }

            val messageWithTestPrefix = if (config.isTestMode) {
                "[TEST MODE - NO EMERGENCY]\n$finalBody"
            } else {
                finalBody
            }

            val parts = smsManager.divideMessage(messageWithTestPrefix)

            for (number in targetNumbers) {
                try {
                    if (parts.size > 1) {
                        smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                    } else {
                        smsManager.sendTextMessage(number, null, messageWithTestPrefix, null, null)
                    }
                    sent++
                } catch (e: Exception) {
                    failed++
                }
            }
        } catch (e: Exception) {
            // Failure-isolated
        }

        return Pair(sent, failed)
    }
}
