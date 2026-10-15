package com.duress.app.pipeline

import android.content.Context
import com.duress.app.data.DuressPreferences
import com.duress.app.data.PipelineConfig
import com.duress.app.data.TriggerConfig
import com.duress.app.trigger.HapticFeedbackHelper
import com.duress.app.trigger.TriggerSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object EmergencyPipelineExecutor {

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Executes the emergency duress pipeline.
     * Rule: Every action is failure-isolated and toggleable.
     * Rule: Log which trigger path fired (key sequence, tile, widget), on-device only.
     */
    fun execute(context: Context, source: TriggerSource, initialDetails: String = "") {
        scope.launch {
            val appContext = context.applicationContext
            val prefs = DuressPreferences(appContext)

            val triggerConfig = try { prefs.triggerConfigFlow.first() } catch (_: Exception) { TriggerConfig() }
            val pipelineConfig = try { prefs.pipelineConfigFlow.first() } catch (_: Exception) { PipelineConfig() }

            val actionsSummary = mutableListOf<String>()

            // 1. Failure-Isolated Haptic Feedback
            try {
                if (triggerConfig?.hapticEnabled != false) {
                    HapticFeedbackHelper.performConfirmHaptic(appContext)
                    actionsSummary.add("Haptic Confirm")
                }
            } catch (_: Exception) {}

            // 2. Failure-Isolated DND Mode (Silence phone)
            try {
                if (pipelineConfig?.actionDndEnabled != false) {
                    val silenced = DndHelper.enableDndSilence(appContext)
                    if (silenced) actionsSummary.add("DND Silenced")
                }
            } catch (_: Exception) {}

            // 3. Failure-Isolated Location & Initial Silent SMS SOS
            var mapsUrl = ""
            try {
                if (pipelineConfig?.actionSmsEnabled != false) {
                    val location = LocationHelper.getFreshLocationWithFallback(appContext, 4000L)
                    mapsUrl = LocationHelper.formatMapsUrl(location)

                    val (sent, failed) = SmsSender.sendSosSms(appContext, pipelineConfig, mapsUrl)
                    if (sent > 0) {
                        actionsSummary.add("SMS Sent ($sent)")
                    } else if (failed > 0) {
                        actionsSummary.add("SMS Failed ($failed)")
                    }
                }
            } catch (_: Exception) {}

            // 4. Failure-Isolated 15-Minute Live Location Tracking Service
            try {
                if (pipelineConfig?.actionLocationTrackingEnabled != false && pipelineConfig?.actionSmsEnabled != false) {
                    SosTrackingService.start(appContext)
                    actionsSummary.add("Live Tracking 15m")
                }
            } catch (_: Exception) {}

            // 5. Failure-Isolated Device Lock (API 28+ Accessibility Lock, API 26-27 Device Admin)
            try {
                if (pipelineConfig?.actionLockEnabled != false) {
                    val locked = DeviceLockHelper.lockDevice(appContext)
                    if (locked) actionsSummary.add("Device Locked")
                }
            } catch (_: Exception) {}

            // 6. Failure-Isolated Log Handling (Clear logs or record audit entry)
            try {
                if (pipelineConfig?.actionClearLogsEnabled == true) {
                    prefs.clearTriggerLogs()
                } else {
                    val detailsText = if (actionsSummary.isNotEmpty()) {
                        "${actionsSummary.joinToString(" • ")} | $initialDetails"
                    } else {
                        initialDetails.ifBlank { source.displayNameEn }
                    }
                    prefs.addTriggerLog(source, detailsText)
                }
            } catch (_: Exception) {}
        }
    }
}
