package com.duress.app.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class DuressDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, DuressDeviceAdminReceiver::class.java)
        }

        fun isActive(context: Context): Boolean {
            return try {
                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                dpm?.isAdminActive(getComponentName(context)) == true
            } catch (e: Exception) {
                false
            }
        }

        fun getActivationIntent(context: Context): Intent {
            return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(context))
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Enables instant screen locking for Android 8.0-8.1 or when accessibility locking is unavailable."
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }
}
