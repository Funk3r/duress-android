package com.duress.app.data

import android.content.Context
import android.view.KeyEvent
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.duress.app.dataStore
import com.duress.app.security.KeystoreHelper
import com.duress.app.trigger.TriggerSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class PinAuthResult {
    REAL_PIN,
    DURESS_PIN,
    INVALID,
    NONE
}

data class TriggerConfig(
    val triggerKeyCode: Int = KeyEvent.KEYCODE_VOLUME_DOWN,
    val sequenceCount: Int = 4,
    val windowDurationMs: Long = 3000L,
    val debounceMs: Long = 120L,
    val hapticEnabled: Boolean = true,
    val stealthDisguise: String = "DEFAULT",
    val appLanguage: String = "en",
    val accidentalGuardsEnabled: Boolean = false,
    val abortWindowSeconds: Int = 0,
    val screenStateFilter: String = "ALL",
    val flagSecureEnabled: Boolean = true
)

data class PipelineConfig(
    val actionSmsEnabled: Boolean = true,
    val actionLocationTrackingEnabled: Boolean = true,
    val actionLockEnabled: Boolean = true,
    val actionDndEnabled: Boolean = true,
    val actionClearLogsEnabled: Boolean = false,
    val isTestMode: Boolean = true,
    val testPhoneNumber: String = "",
    val emergencyContacts: Set<String> = emptySet(),
    val customSmsTemplate: String = ""
)

data class SecurityConfig(
    val pinProtectionEnabled: Boolean = false,
    val encryptedRealPinHash: String = "",
    val encryptedDuressPinHash: String = "",
    val decoyTheme: String = "NOTES" // "NOTES", "CALC", "CLEAN"
)

class DuressPreferences(private val context: Context) {

    private object PreferencesKeys {
        // Trigger Settings
        val TRIGGER_KEY_CODE = intPreferencesKey("trigger_key_code")
        val SEQUENCE_COUNT = intPreferencesKey("sequence_count")
        val WINDOW_DURATION_MS = longPreferencesKey("window_duration_ms")
        val DEBOUNCE_MS = longPreferencesKey("debounce_ms")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val STEALTH_DISGUISE = stringPreferencesKey("stealth_disguise")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val TRIGGER_LOGS = stringSetPreferencesKey("trigger_logs")

        // Pipeline Settings (Phase 2)
        val ACTION_SMS_ENABLED = booleanPreferencesKey("action_sms_enabled")
        val ACTION_LOCATION_TRACKING_ENABLED = booleanPreferencesKey("action_location_tracking_enabled")
        val ACTION_LOCK_ENABLED = booleanPreferencesKey("action_lock_enabled")
        val ACTION_DND_ENABLED = booleanPreferencesKey("action_dnd_enabled")
        val ACTION_CLEAR_LOGS_ENABLED = booleanPreferencesKey("action_clear_logs_enabled")
        val IS_TEST_MODE = booleanPreferencesKey("is_test_mode")
        val TEST_PHONE_NUMBER = stringPreferencesKey("test_phone_number")
        val EMERGENCY_CONTACTS = stringSetPreferencesKey("emergency_contacts")
        val CUSTOM_SMS_TEMPLATE = stringPreferencesKey("custom_sms_template")

        // Security & Decoy Settings (Phase 3)
        val PIN_PROTECTION_ENABLED = booleanPreferencesKey("pin_protection_enabled")
        val ENCRYPTED_REAL_PIN_HASH = stringPreferencesKey("encrypted_real_pin_hash")
        val ENCRYPTED_DURESS_PIN_HASH = stringPreferencesKey("encrypted_duress_pin_hash")
        val DECOY_THEME = stringPreferencesKey("decoy_theme")

        // App Protection Checklist (Phase 4)
        val PROTECTED_APPS_CHECKLIST = stringSetPreferencesKey("protected_apps_checklist")

        // Money Guide Checklist (Phase 5)
        val MONEY_GUIDE_CHECKLIST = stringSetPreferencesKey("money_guide_checklist")

        // Hardening & Accidental Guards (Phase 6)
        val ACCIDENTAL_GUARDS_ENABLED = booleanPreferencesKey("accidental_guards_enabled")
        val ABORT_WINDOW_SECONDS = intPreferencesKey("abort_window_seconds")
        val SCREEN_STATE_FILTER = stringPreferencesKey("screen_state_filter")
        val FLAG_SECURE_ENABLED = booleanPreferencesKey("flag_secure_enabled")
    }

    val triggerConfigFlow: Flow<TriggerConfig> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            TriggerConfig(
                triggerKeyCode = preferences[PreferencesKeys.TRIGGER_KEY_CODE] ?: KeyEvent.KEYCODE_VOLUME_DOWN,
                sequenceCount = preferences[PreferencesKeys.SEQUENCE_COUNT] ?: 4,
                windowDurationMs = preferences[PreferencesKeys.WINDOW_DURATION_MS] ?: 3000L,
                debounceMs = preferences[PreferencesKeys.DEBOUNCE_MS] ?: 120L,
                hapticEnabled = preferences[PreferencesKeys.HAPTIC_ENABLED] ?: true,
                stealthDisguise = preferences[PreferencesKeys.STEALTH_DISGUISE] ?: "DEFAULT",
                appLanguage = preferences[PreferencesKeys.APP_LANGUAGE] ?: "en",
                accidentalGuardsEnabled = preferences[PreferencesKeys.ACCIDENTAL_GUARDS_ENABLED] ?: false,
                abortWindowSeconds = preferences[PreferencesKeys.ABORT_WINDOW_SECONDS] ?: 0,
                screenStateFilter = preferences[PreferencesKeys.SCREEN_STATE_FILTER] ?: "ALL",
                flagSecureEnabled = preferences[PreferencesKeys.FLAG_SECURE_ENABLED] ?: true
            )
        }

    val pipelineConfigFlow: Flow<PipelineConfig> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            PipelineConfig(
                actionSmsEnabled = preferences[PreferencesKeys.ACTION_SMS_ENABLED] ?: true,
                actionLocationTrackingEnabled = preferences[PreferencesKeys.ACTION_LOCATION_TRACKING_ENABLED] ?: true,
                actionLockEnabled = preferences[PreferencesKeys.ACTION_LOCK_ENABLED] ?: true,
                actionDndEnabled = preferences[PreferencesKeys.ACTION_DND_ENABLED] ?: true,
                actionClearLogsEnabled = preferences[PreferencesKeys.ACTION_CLEAR_LOGS_ENABLED] ?: false,
                isTestMode = preferences[PreferencesKeys.IS_TEST_MODE] ?: true,
                testPhoneNumber = preferences[PreferencesKeys.TEST_PHONE_NUMBER] ?: "",
                emergencyContacts = preferences[PreferencesKeys.EMERGENCY_CONTACTS] ?: emptySet(),
                customSmsTemplate = preferences[PreferencesKeys.CUSTOM_SMS_TEMPLATE] ?: ""
            )
        }

    val securityConfigFlow: Flow<SecurityConfig> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            SecurityConfig(
                pinProtectionEnabled = preferences[PreferencesKeys.PIN_PROTECTION_ENABLED] ?: false,
                encryptedRealPinHash = preferences[PreferencesKeys.ENCRYPTED_REAL_PIN_HASH] ?: "",
                encryptedDuressPinHash = preferences[PreferencesKeys.ENCRYPTED_DURESS_PIN_HASH] ?: "",
                decoyTheme = preferences[PreferencesKeys.DECOY_THEME] ?: "NOTES"
            )
        }

    val triggerLogsFlow: Flow<List<TriggerLogEntry>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val set = preferences[PreferencesKeys.TRIGGER_LOGS] ?: emptySet()
            set.mapNotNull { TriggerLogEntry.deserialize(it) }
                .sortedByDescending { it.timestamp }
        }

    val protectedAppsFlow: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.PROTECTED_APPS_CHECKLIST] ?: emptySet()
        }

    val moneyGuideFlow: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.MONEY_GUIDE_CHECKLIST] ?: emptySet()
        }

    suspend fun updateTriggerKey(keyCode: Int) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.TRIGGER_KEY_CODE] = keyCode
            }
        } catch (_: Exception) {}
    }

    suspend fun updateSequenceCount(count: Int) {
        try {
            val clamped = count.coerceIn(3, 6)
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.SEQUENCE_COUNT] = clamped
            }
        } catch (_: Exception) {}
    }

    suspend fun updateWindowDuration(durationMs: Long) {
        try {
            val clamped = durationMs.coerceIn(1500L, 5000L)
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.WINDOW_DURATION_MS] = clamped
            }
        } catch (_: Exception) {}
    }

    suspend fun updateDebounce(debounceMs: Long) {
        try {
            val clamped = debounceMs.coerceIn(50L, 300L)
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.DEBOUNCE_MS] = clamped
            }
        } catch (_: Exception) {}
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.HAPTIC_ENABLED] = enabled
            }
        } catch (_: Exception) {}
    }

    suspend fun setStealthDisguise(disguise: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.STEALTH_DISGUISE] = disguise
            }
        } catch (_: Exception) {}
    }

    suspend fun setAppLanguage(lang: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.APP_LANGUAGE] = lang
            }
        } catch (_: Exception) {}
    }

    // Pipeline settings updates
    suspend fun setActionSmsEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.ACTION_SMS_ENABLED] = enabled
            }
        } catch (_: Exception) {}
    }

    suspend fun setActionLocationTrackingEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.ACTION_LOCATION_TRACKING_ENABLED] = enabled
            }
        } catch (_: Exception) {}
    }

    suspend fun setActionLockEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.ACTION_LOCK_ENABLED] = enabled
            }
        } catch (_: Exception) {}
    }

    suspend fun setActionDndEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.ACTION_DND_ENABLED] = enabled
            }
        } catch (_: Exception) {}
    }

    suspend fun setActionClearLogsEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.ACTION_CLEAR_LOGS_ENABLED] = enabled
            }
        } catch (_: Exception) {}
    }

    suspend fun setIsTestMode(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.IS_TEST_MODE] = enabled
            }
        } catch (_: Exception) {}
    }

    suspend fun setTestPhoneNumber(number: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.TEST_PHONE_NUMBER] = number.trim()
            }
        } catch (_: Exception) {}
    }

    suspend fun addEmergencyContact(number: String) {
        val cleanNumber = number.trim().replace(" ", "").replace("-", "")
        if (cleanNumber.isBlank()) return
        try {
            context.dataStore.edit { preferences ->
                val current = preferences[PreferencesKeys.EMERGENCY_CONTACTS]?.toMutableSet() ?: mutableSetOf()
                current.add(cleanNumber)
                preferences[PreferencesKeys.EMERGENCY_CONTACTS] = current
            }
        } catch (_: Exception) {}
    }

    suspend fun removeEmergencyContact(number: String) {
        try {
            context.dataStore.edit { preferences ->
                val current = preferences[PreferencesKeys.EMERGENCY_CONTACTS]?.toMutableSet() ?: mutableSetOf()
                current.remove(number)
                preferences[PreferencesKeys.EMERGENCY_CONTACTS] = current
            }
        } catch (_: Exception) {}
    }

    suspend fun setCustomSmsTemplate(template: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.CUSTOM_SMS_TEMPLATE] = template.trim()
            }
        } catch (_: Exception) {}
    }

    // Security & Decoy Settings (Phase 3)
    suspend fun setPinProtectionEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.PIN_PROTECTION_ENABLED] = enabled
            }
        } catch (_: Exception) {}
    }

    suspend fun configurePins(realPin: String, duressPin: String): Boolean {
        if (realPin.length < 4 || duressPin.length < 4 || realPin == duressPin) {
            return false
        }
        return try {
            val encryptedReal = KeystoreHelper.hashAndEncryptPin(realPin)
            val encryptedDuress = KeystoreHelper.hashAndEncryptPin(duressPin)
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.PIN_PROTECTION_ENABLED] = true
                preferences[PreferencesKeys.ENCRYPTED_REAL_PIN_HASH] = encryptedReal
                preferences[PreferencesKeys.ENCRYPTED_DURESS_PIN_HASH] = encryptedDuress
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun setDecoyTheme(theme: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.DECOY_THEME] = theme
            }
        } catch (_: Exception) {}
    }

    suspend fun verifyEnteredPin(enteredPin: String): PinAuthResult {
        return try {
            val sec = securityConfigFlow.first()
            if (!sec.pinProtectionEnabled) {
                return PinAuthResult.NONE
            }
            if (KeystoreHelper.verifyPin(enteredPin, sec.encryptedRealPinHash)) {
                PinAuthResult.REAL_PIN
            } else if (KeystoreHelper.verifyPin(enteredPin, sec.encryptedDuressPinHash)) {
                PinAuthResult.DURESS_PIN
            } else {
                PinAuthResult.INVALID
            }
        } catch (e: Exception) {
            PinAuthResult.INVALID
        }
    }

    suspend fun addTriggerLog(source: TriggerSource, details: String) {
        try {
            val newEntry = TriggerLogEntry(source = source, details = details)
            context.dataStore.edit { preferences ->
                val current = preferences[PreferencesKeys.TRIGGER_LOGS]?.toMutableSet() ?: mutableSetOf()
                current.add(newEntry.serialize())
                if (current.size > 50) {
                    val parsed = current.mapNotNull { TriggerLogEntry.deserialize(it) }
                        .sortedByDescending { it.timestamp }
                        .take(50)
                    preferences[PreferencesKeys.TRIGGER_LOGS] = parsed.map { it.serialize() }.toSet()
                } else {
                    preferences[PreferencesKeys.TRIGGER_LOGS] = current
                }
            }
        } catch (_: Exception) {}
    }

    suspend fun clearTriggerLogs() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(PreferencesKeys.TRIGGER_LOGS)
            }
        } catch (_: Exception) {}
    }

    // App Protection Checklist (Phase 4)
    suspend fun toggleProtectedApp(appId: String, isProtected: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                val current = preferences[PreferencesKeys.PROTECTED_APPS_CHECKLIST]?.toMutableSet() ?: mutableSetOf()
                if (isProtected) {
                    current.add(appId)
                } else {
                    current.remove(appId)
                }
                preferences[PreferencesKeys.PROTECTED_APPS_CHECKLIST] = current
            }
        } catch (_: Exception) {}
    }

    suspend fun resetProtectedApps() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(PreferencesKeys.PROTECTED_APPS_CHECKLIST)
            }
        } catch (_: Exception) {}
    }

    // Money Guide Checklist (Phase 5)
    suspend fun toggleMoneyGuideItem(itemId: String, isCompleted: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                val current = preferences[PreferencesKeys.MONEY_GUIDE_CHECKLIST]?.toMutableSet() ?: mutableSetOf()
                if (isCompleted) {
                    current.add(itemId)
                } else {
                    current.remove(itemId)
                }
                preferences[PreferencesKeys.MONEY_GUIDE_CHECKLIST] = current
            }
        } catch (_: Exception) {}
    }

    suspend fun resetMoneyGuide() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(PreferencesKeys.MONEY_GUIDE_CHECKLIST)
            }
        } catch (_: Exception) {}
    }

    // Hardening & Accidental Guards (Phase 6)
    suspend fun setAccidentalGuardsEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.ACCIDENTAL_GUARDS_ENABLED] = enabled
            }
        } catch (_: Exception) {}
    }

    suspend fun setAbortWindowSeconds(seconds: Int) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.ABORT_WINDOW_SECONDS] = seconds
            }
        } catch (_: Exception) {}
    }

    suspend fun setScreenStateFilter(filter: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.SCREEN_STATE_FILTER] = filter
            }
        } catch (_: Exception) {}
    }

    suspend fun setFlagSecureEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.FLAG_SECURE_ENABLED] = enabled
            }
        } catch (_: Exception) {}
    }
}
