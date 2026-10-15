package com.duress.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.duress.app.data.DuressPreferences
import com.duress.app.data.TriggerConfig
import com.duress.app.trigger.HapticFeedbackHelper
import com.duress.app.trigger.TriggerManager
import com.duress.app.trigger.TriggerSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.LinkedList

class DuressAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var configCollectorJob: Job? = null

    @Volatile
    private var currentConfig = TriggerConfig()

    @Volatile
    private var pendingTriggerJob: Job? = null

    // Thread-safe list of recent key down timestamps
    private val keyPressTimestamps = Collections.synchronizedList(LinkedList<Long>())

    @Volatile
    private var lastRecordedDownTime: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        instance = this

        val prefs = DuressPreferences(applicationContext)
        configCollectorJob = serviceScope.launch {
            prefs.triggerConfigFlow.collectLatest { config ->
                currentConfig = config
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        try {
            // Only examine ACTION_DOWN events
            if (event.action != KeyEvent.ACTION_DOWN) {
                return super.onKeyEvent(event)
            }

            // Reject key repeats (holding button down is not repeated presses)
            if (event.repeatCount > 0) {
                return super.onKeyEvent(event)
            }

            val keyCode = event.keyCode
            val config = currentConfig

            // Check if opposite key is pressed while abort window is active
            val oppositeKey = if (config.triggerKeyCode == KeyEvent.KEYCODE_VOLUME_DOWN) KeyEvent.KEYCODE_VOLUME_UP else KeyEvent.KEYCODE_VOLUME_DOWN
            if (keyCode == oppositeKey && pendingTriggerJob?.isActive == true) {
                pendingTriggerJob?.cancel()
                pendingTriggerJob = null
                HapticFeedbackHelper.performSinglePulse(this@DuressAccessibilityService)
                serviceScope.launch {
                    val prefs = DuressPreferences(applicationContext)
                    prefs.addTriggerLog(TriggerSource.KEY_SEQUENCE, "Trigger aborted by user via abort key window")
                }
                return super.onKeyEvent(event)
            }

            // Screen state accidental guard
            if (config.accidentalGuardsEnabled) {
                val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (pm != null) {
                    val isInteractive = pm.isInteractive
                    if (config.screenStateFilter == "SCREEN_OFF_ONLY" && isInteractive) {
                        return super.onKeyEvent(event)
                    }
                    if (config.screenStateFilter == "SCREEN_ON_ONLY" && !isInteractive) {
                        return super.onKeyEvent(event)
                    }
                }
            }

            // Check if key matches configured trigger key
            if (keyCode == config.triggerKeyCode) {
                val now = System.currentTimeMillis()

                // Debounce check: reject presses occurring too quickly after the previous one
                if (now - lastRecordedDownTime < config.debounceMs) {
                    return super.onKeyEvent(event)
                }
                lastRecordedDownTime = now

                synchronized(keyPressTimestamps) {
                    keyPressTimestamps.add(now)

                    // Prune presses that occurred outside the time window
                    val cutoff = now - config.windowDurationMs
                    keyPressTimestamps.removeAll { it < cutoff }

                    // Check if required sequence count is reached
                    if (keyPressTimestamps.size >= config.sequenceCount) {
                        val firstPress = keyPressTimestamps.firstOrNull() ?: now
                        val elapsedSeconds = (now - firstPress) / 1000f
                        keyPressTimestamps.clear()

                        val keyName = if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) "Vol Down" else "Vol Up"
                        val details = "$keyName x${config.sequenceCount} (${String.format("%.1f", elapsedSeconds)}s)"

                        if (config.abortWindowSeconds > 0) {
                            pendingTriggerJob?.cancel()
                            HapticFeedbackHelper.performConfirmHaptic(this@DuressAccessibilityService)
                            pendingTriggerJob = serviceScope.launch {
                                delay(config.abortWindowSeconds * 1000L)
                                TriggerManager.onTrigger(
                                    context = this@DuressAccessibilityService,
                                    source = TriggerSource.KEY_SEQUENCE,
                                    details = details
                                )
                                pendingTriggerJob = null
                            }
                        } else {
                            TriggerManager.onTrigger(
                                context = this@DuressAccessibilityService,
                                source = TriggerSource.KEY_SEQUENCE,
                                details = details
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Failure-isolated: never crash the accessibility service
        }

        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used; only key filtering is needed
    }

    override fun onInterrupt() {
        pendingTriggerJob?.cancel()
        pendingTriggerJob = null
        keyPressTimestamps.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        if (instance === this) {
            instance = null
        }
        configCollectorJob?.cancel()
        pendingTriggerJob?.cancel()
        pendingTriggerJob = null
        keyPressTimestamps.clear()
    }

    companion object {
        @Volatile
        var isRunning: Boolean = false
            private set

        @Volatile
        private var instance: DuressAccessibilityService? = null

        /**
         * Locks screen using AccessibilityService GLOBAL_ACTION_LOCK_SCREEN (Android 9+ / API 28+)
         */
        fun lockScreenViaAccessibility(): Boolean {
            return try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val service = instance
                    if (service != null) {
                        return service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Checks if the Accessibility Service is currently enabled in Android system settings.
         */
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            return try {
                if (isRunning) return true
                val expectedComponentName = "${context.packageName}/${DuressAccessibilityService::class.java.canonicalName}"
                val enabledServicesSetting = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: return false

                val colonSplitter = TextUtils.SimpleStringSplitter(':')
                colonSplitter.setString(enabledServicesSetting)

                while (colonSplitter.hasNext()) {
                    val componentName = colonSplitter.next()
                    if (componentName.equals(expectedComponentName, ignoreCase = true)) {
                        return true
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }
    }
}
