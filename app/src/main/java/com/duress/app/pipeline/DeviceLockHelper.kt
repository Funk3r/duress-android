package com.duress.app.pipeline

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import com.duress.app.admin.DuressDeviceAdminReceiver
import com.duress.app.service.DuressAccessibilityService

object DeviceLockHelper {

    /**
     * Rule: API 28+: GLOBAL_ACTION_LOCK_SCREEN. API 26-27 or if unavailable: DevicePolicyManager.lockNow via optional device admin.
     * Failure-isolated: never crashes.
     */
    fun lockDevice(context: Context): Boolean {
        // 1. First priority for API 28+ (Android 9-15): AccessibilityService GLOBAL_ACTION_LOCK_SCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val locked = DuressAccessibilityService.lockScreenViaAccessibility()
            if (locked) return true
        }

        // 2. Second priority (API 26-27 or if accessibility lock failed): DevicePolicyManager.lockNow()
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            if (dpm != null && DuressDeviceAdminReceiver.isActive(context)) {
                dpm.lockNow()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
