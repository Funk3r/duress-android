package com.duress.app.hardening

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import com.duress.app.R
import com.duress.app.protection.OemBrand

data class KeepAliveBrandGuide(
    val brand: OemBrand,
    @StringRes val brandTitleRes: Int,
    @StringRes val brandSummaryRes: Int,
    @StringRes val stepsRes: Int,
    @StringRes val actionBtnRes: Int,
    val onLaunchSettings: (Context) -> Unit
)

object KeepAliveWizardHelper {

    fun getBrandGuide(brand: OemBrand): KeepAliveBrandGuide {
        return when (brand) {
            OemBrand.TRANSSION -> KeepAliveBrandGuide(
                brand = OemBrand.TRANSSION,
                brandTitleRes = R.string.keepalive_transsion_title,
                brandSummaryRes = R.string.keepalive_transsion_summary,
                stepsRes = R.string.keepalive_transsion_steps,
                actionBtnRes = R.string.keepalive_transsion_btn,
                onLaunchSettings = { context -> openTranssionAutoStart(context) }
            )
            OemBrand.XIAOMI -> KeepAliveBrandGuide(
                brand = OemBrand.XIAOMI,
                brandTitleRes = R.string.keepalive_xiaomi_title,
                brandSummaryRes = R.string.keepalive_xiaomi_summary,
                stepsRes = R.string.keepalive_xiaomi_steps,
                actionBtnRes = R.string.keepalive_xiaomi_btn,
                onLaunchSettings = { context -> openXiaomiAutoStart(context) }
            )
            OemBrand.VIVO -> KeepAliveBrandGuide(
                brand = OemBrand.VIVO,
                brandTitleRes = R.string.keepalive_vivo_title,
                brandSummaryRes = R.string.keepalive_vivo_summary,
                stepsRes = R.string.keepalive_vivo_steps,
                actionBtnRes = R.string.keepalive_vivo_btn,
                onLaunchSettings = { context -> openVivoAutoStart(context) }
            )
            OemBrand.OPPO -> KeepAliveBrandGuide(
                brand = OemBrand.OPPO,
                brandTitleRes = R.string.keepalive_oppo_title,
                brandSummaryRes = R.string.keepalive_oppo_summary,
                stepsRes = R.string.keepalive_oppo_steps,
                actionBtnRes = R.string.keepalive_oppo_btn,
                onLaunchSettings = { context -> openOppoAutoStart(context) }
            )
            OemBrand.SAMSUNG -> KeepAliveBrandGuide(
                brand = OemBrand.SAMSUNG,
                brandTitleRes = R.string.keepalive_samsung_title,
                brandSummaryRes = R.string.keepalive_samsung_summary,
                stepsRes = R.string.keepalive_samsung_steps,
                actionBtnRes = R.string.keepalive_samsung_btn,
                onLaunchSettings = { context -> openSamsungBatterySettings(context) }
            )
            else -> KeepAliveBrandGuide(
                brand = OemBrand.GENERIC,
                brandTitleRes = R.string.keepalive_generic_title,
                brandSummaryRes = R.string.keepalive_generic_summary,
                stepsRes = R.string.keepalive_generic_steps,
                actionBtnRes = R.string.keepalive_generic_btn,
                onLaunchSettings = { context -> BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context) }
            )
        }
    }

    private fun openTranssionAutoStart(context: Context) {
        val candidates = listOfNotNull(
            context.packageManager.getLaunchIntentForPackage("com.transsion.phonemaster"),
            Intent().setComponent(ComponentName("com.transsion.phonemaster", "com.transsion.phonemaster.MainActivity")),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        )
        for (intent in candidates) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
    }

    private fun openXiaomiAutoStart(context: Context) {
        val candidates = listOfNotNull(
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            Intent().setComponent(ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")),
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.securityscan.MainActivity")),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        )
        for (intent in candidates) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
    }

    private fun openVivoAutoStart(context: Context) {
        val candidates = listOfNotNull(
            Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            context.packageManager.getLaunchIntentForPackage("com.iqoo.secure"),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        )
        for (intent in candidates) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
    }

    private fun openOppoAutoStart(context: Context) {
        val candidates = listOfNotNull(
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.oplus.safecenter", "com.oplus.safecenter.permission.startup.StartupAppListActivity")),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        )
        for (intent in candidates) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
    }

    private fun openSamsungBatterySettings(context: Context) {
        val candidates = listOfNotNull(
            Intent().setComponent(ComponentName("com.samsung.android.sm", "com.samsung.android.sm.battery.ui.BatteryActivity")),
            Intent().setComponent(ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        )
        for (intent in candidates) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
    }
}
