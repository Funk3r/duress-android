package com.duress.app.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import android.view.WindowManager
import com.duress.app.data.DuressPreferences
import com.duress.app.data.PinAuthResult
import com.duress.app.data.SecurityConfig
import com.duress.app.data.TriggerConfig
import com.duress.app.pipeline.EmergencyPipelineExecutor
import com.duress.app.service.DuressAccessibilityService
import com.duress.app.trigger.TriggerSource
import com.duress.app.ui.screens.DecoyScreen
import com.duress.app.ui.screens.HomeScreen
import com.duress.app.ui.screens.PinEntryScreen
import com.duress.app.ui.theme.DuressTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

enum class AppAuthState {
    CHECKING,
    LOCKED,
    AUTHENTICATED_REAL,
    AUTHENTICATED_DECOY
}

class MainActivity : ComponentActivity() {

    private var isAccessibilityActive by mutableStateOf(false)
    private lateinit var preferences: DuressPreferences

    override fun attachBaseContext(newBase: Context) {
        val prefs = DuressPreferences(newBase)
        val lang = try {
            "en"
        } catch (_: Exception) {
            "en"
        }
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = DuressPreferences(this)

        updateAccessibilityState()

        setContent {
            DuressTheme {
                val securityConfig by preferences.securityConfigFlow.collectAsState(initial = SecurityConfig())
                val triggerConfig by preferences.triggerConfigFlow.collectAsState(initial = TriggerConfig())
                var authState by remember { mutableStateOf(AppAuthState.CHECKING) }

                LaunchedEffect(triggerConfig.flagSecureEnabled) {
                    if (triggerConfig.flagSecureEnabled) {
                        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }

                LaunchedEffect(securityConfig) {
                    if (!securityConfig.pinProtectionEnabled) {
                        authState = AppAuthState.AUTHENTICATED_REAL
                    } else if (authState == AppAuthState.CHECKING) {
                        authState = AppAuthState.LOCKED
                    }
                }

                when (authState) {
                    AppAuthState.CHECKING -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    AppAuthState.LOCKED -> {
                        PinEntryScreen(
                            onPinSubmitted = { pin ->
                                lifecycleScope.launch {
                                    val result = preferences.verifyEnteredPin(pin)
                                    when (result) {
                                        PinAuthResult.REAL_PIN -> {
                                            authState = AppAuthState.AUTHENTICATED_REAL
                                        }
                                        PinAuthResult.DURESS_PIN -> {
                                            // Silently execute emergency duress pipeline!
                                            EmergencyPipelineExecutor.execute(
                                                context = this@MainActivity,
                                                source = TriggerSource.DECOY_PIN,
                                                initialDetails = "Duress PIN verified at app entry"
                                            )
                                            authState = AppAuthState.AUTHENTICATED_DECOY
                                        }
                                        PinAuthResult.NONE -> {
                                            authState = AppAuthState.AUTHENTICATED_REAL
                                        }
                                        PinAuthResult.INVALID -> {
                                            // PinEntryScreen will display error
                                        }
                                    }
                                }
                            }
                        )
                    }
                    AppAuthState.AUTHENTICATED_DECOY -> {
                        DecoyScreen(decoyTheme = securityConfig.decoyTheme)
                    }
                    AppAuthState.AUTHENTICATED_REAL -> {
                        HomeScreen(
                            isAccessibilityActive = isAccessibilityActive,
                            onLanguageToggle = {
                                lifecycleScope.launch {
                                    val current = preferences.triggerConfigFlow.first().appLanguage
                                    val newLang = if (current == "ur") "en" else "ur"
                                    preferences.setAppLanguage(newLang)
                                    updateAppLocale(newLang)
                                    recreate()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAccessibilityState()
    }

    private fun updateAccessibilityState() {
        isAccessibilityActive = DuressAccessibilityService.isAccessibilityServiceEnabled(this)
    }

    @Suppress("DEPRECATION")
    private fun updateAppLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
