package com.duress.app.protection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

enum class OemBrand(val displayName: String, val brandKeys: List<String>) {
    TRANSSION("Infinix / Tecno / Itel", listOf("infinix", "tecno", "itel", "transsion")),
    SAMSUNG("Samsung Galaxy", listOf("samsung")),
    XIAOMI("Xiaomi / Redmi / Poco", listOf("xiaomi", "redmi", "poco")),
    VIVO("Vivo / iQOO", listOf("vivo", "iqoo")),
    OPPO("Oppo / Realme / OnePlus", listOf("oppo", "realme", "oneplus")),
    GOOGLE("Google Pixel", listOf("google")),
    GENERIC("Android Device", emptyList())
}

object AppProtectionHelper {

    fun detectOemBrand(): OemBrand {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        for (item in OemBrand.entries) {
            if (item.brandKeys.any { manufacturer.contains(it) || brand.contains(it) }) {
                return item
            }
        }
        return OemBrand.GENERIC
    }

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    fun isAnyPackageInstalled(context: Context, packageNames: List<String>): String? {
        return packageNames.firstOrNull { isPackageInstalled(context, it) }
    }

    fun isShelterInstalled(context: Context): Boolean = isPackageInstalled(context, "net.typeblog.shelter")
    fun isIslandInstalled(context: Context): Boolean = isPackageInstalled(context, "com.oasisfeng.island")

    fun launchAppOrDetails(context: Context, packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                val appInfoIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(appInfoIntent)
                true
            }
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open $packageName", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun openPrivateSpaceSettings(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 35) {
            try {
                val intent = Intent("android.settings.PRIVATE_SPACE_SETTINGS").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            } catch (_: Exception) {}
        }
        return openPrivacySettings(context)
    }

    fun openSamsungSecureFolder(context: Context): Boolean {
        val candidates = listOf(
            Intent().setComponent(ComponentName("com.samsung.knox.securefolder", "com.samsung.knox.securefolder.switcher.SwitchActivity")),
            Intent("com.samsung.knox.securefolder.LAUNCH"),
            context.packageManager.getLaunchIntentForPackage("com.samsung.knox.securefolder")
        )
        for (intent in candidates) {
            if (intent != null) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                } catch (_: Exception) {}
            }
        }
        return openSecuritySettings(context)
    }

    fun openTranssionXHide(context: Context): Boolean {
        val packages = listOf("com.transsion.phonemaster", "com.transsion.xhide", "com.transsion.hilauncher")
        for (pkg in packages) {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                } catch (_: Exception) {}
            }
        }
        // Fallback: Open phone dialer with ## pre-filled to hint user's secret code
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:%23%23")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
            Toast.makeText(context, "Dial your XHide code (e.g. ##1234)", Toast.LENGTH_LONG).show()
            return true
        } catch (_: Exception) {}

        return openSecuritySettings(context)
    }

    fun openXiaomiSecondSpace(context: Context): Boolean {
        val intents = listOf(
            Intent().setComponent(ComponentName("com.miui.securitycore", "com.miui.enterprise.service.EntSecondSpace")),
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.securityscan.MainActivity")),
            context.packageManager.getLaunchIntentForPackage("com.miui.securitycenter")
        )
        for (intent in intents) {
            if (intent != null) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                } catch (_: Exception) {}
            }
        }
        return openSecuritySettings(context)
    }

    fun openVivoAppHide(context: Context): Boolean {
        val packages = listOf("com.iqoo.secure", "com.vivo.permissionmanager")
        for (pkg in packages) {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                } catch (_: Exception) {}
            }
        }
        return openSecuritySettings(context)
    }

    fun openOppoPrivateSafe(context: Context): Boolean {
        val packages = listOf("com.coloros.safecenter", "com.oplus.safecenter")
        for (pkg in packages) {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                } catch (_: Exception) {}
            }
        }
        return openPrivacySettings(context)
    }

    fun openShelterOrIsland(context: Context): Boolean {
        if (isShelterInstalled(context)) {
            val intent = context.packageManager.getLaunchIntentForPackage("net.typeblog.shelter")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            }
        }
        if (isIslandInstalled(context)) {
            val intent = context.packageManager.getLaunchIntentForPackage("com.oasisfeng.island")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            }
        }
        // Open Play Store or F-Droid URL
        return try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.typeblog.shelter")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
            true
        } catch (_: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/net.typeblog.shelter/")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                true
            } catch (_: Exception) {
                Toast.makeText(context, "Please install Shelter from F-Droid or Play Store", Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    fun openSecuritySettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            openSystemSettings(context)
        }
    }

    fun openPrivacySettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_PRIVACY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            openSecuritySettings(context)
        }
    }

    fun openNotificationSettings(context: Context): Boolean {
        return try {
            val intent = Intent("android.settings.NOTIFICATION_SETTINGS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            try {
                val soundIntent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(soundIntent)
                true
            } catch (_: Exception) {
                openSystemSettings(context)
            }
        }
    }

    private fun openSystemSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open system settings", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
