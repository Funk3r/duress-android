package com.duress.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.duress.app.data.DuressPreferences
import com.duress.app.service.DuressAccessibilityService
import com.duress.app.trigger.TriggerSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = DuressPreferences(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isServiceActive = DuressAccessibilityService.isAccessibilityServiceEnabled(context)
                    val statusText = if (isServiceActive) "Active" else "Inactive"
                    prefs.addTriggerLog(
                        source = TriggerSource.SYSTEM_BOOT,
                        details = "Boot completed ($action). Accessibility: $statusText"
                    )
                } catch (_: Exception) {}
            }
        }
    }
}
