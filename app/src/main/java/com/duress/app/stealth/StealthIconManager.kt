package com.duress.app.stealth

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthIconManager {

    enum class Disguise(val id: String, val aliasClassName: String, val labelEn: String, val labelUr: String) {
        DEFAULT("DEFAULT", "com.duress.app.LauncherDefault", "Duress (Default)", "ڈیوریس (اصل)"),
        CALCULATOR("CALCULATOR", "com.duress.app.LauncherCalculator", "Calculator", "کیلکولیٹر"),
        NOTES("NOTES", "com.duress.app.LauncherNotes", "Quick Notes", "نوٹس"),
        CLOCK("CLOCK", "com.duress.app.LauncherClock", "Clock", "گھڑی");

        companion object {
            fun fromId(id: String): Disguise = entries.firstOrNull { it.id == id } ?: DEFAULT
        }
    }

    /**
     * Toggles launcher activity-aliases to disguise the app.
     * Uses DONT_KILL_APP to avoid crashing while running.
     */
    fun applyDisguise(context: Context, disguise: Disguise): Boolean {
        return try {
            val pm = context.packageManager
            val pkg = context.packageName

            for (item in Disguise.entries) {
                val component = ComponentName(pkg, item.aliasClassName)
                val newState = if (item == disguise) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }

                pm.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
