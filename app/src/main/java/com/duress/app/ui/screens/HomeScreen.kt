package com.duress.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.duress.app.data.SecurityConfig
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.duress.app.R
import com.duress.app.admin.DuressDeviceAdminReceiver
import com.duress.app.data.DuressPreferences
import com.duress.app.data.PipelineConfig
import com.duress.app.data.TriggerConfig
import com.duress.app.data.TriggerLogEntry
import com.duress.app.pipeline.DndHelper
import com.duress.app.pipeline.LocationHelper
import com.duress.app.pipeline.SmsSender
import com.duress.app.stealth.StealthIconManager
import com.duress.app.trigger.HapticFeedbackHelper
import com.duress.app.trigger.TriggerManager
import com.duress.app.trigger.TriggerSource
import com.duress.app.ui.theme.SafeGreen
import com.duress.app.ui.theme.UrgentRed
import com.duress.app.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isAccessibilityActive: Boolean,
    onLanguageToggle: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferences = remember { DuressPreferences(context) }

    val triggerConfig by preferences.triggerConfigFlow.collectAsState(initial = TriggerConfig())
    val pipelineConfig by preferences.pipelineConfigFlow.collectAsState(initial = PipelineConfig())
    val logs by preferences.triggerLogsFlow.collectAsState(initial = emptyList())

    val isUrdu = triggerConfig.appLanguage == "ur"
    val layoutDirection = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = stringResource(R.string.app_tagline),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        OutlinedButton(
                            onClick = onLanguageToggle,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (isUrdu) "English" else "اردو",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Test Mode Indicator Banner across top of app
                if (pipelineConfig.isTestMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarningAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.test_mode_banner),
                                color = WarningAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    edgePadding = 8.dp
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text(stringResource(R.string.tab_triggers)) },
                        icon = { Icon(Icons.Default.TouchApp, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text(stringResource(R.string.tab_pipeline)) },
                        icon = { Icon(Icons.Default.Security, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text(stringResource(R.string.tab_protect_apps)) },
                        icon = { Icon(Icons.Default.Shield, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 3,
                        onClick = { selectedTabIndex = 3 },
                        text = { Text(stringResource(R.string.tab_money_guide)) },
                        icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 4,
                        onClick = { selectedTabIndex = 4 },
                        text = { Text(stringResource(R.string.tab_hardening)) },
                        icon = { Icon(Icons.Default.BatteryChargingFull, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 5,
                        onClick = { selectedTabIndex = 5 },
                        text = { Text(stringResource(R.string.tab_logs)) },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 6,
                        onClick = { selectedTabIndex = 6 },
                        text = { Text(stringResource(R.string.tab_settings)) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                }

                when (selectedTabIndex) {
                    0 -> TriggersTabContent(
                        context = context,
                        config = triggerConfig,
                        isAccessibilityActive = isAccessibilityActive,
                        preferences = preferences
                    )
                    1 -> PipelineTabContent(
                        context = context,
                        pipelineConfig = pipelineConfig,
                        preferences = preferences
                    )
                    2 -> ProtectAppsScreen(
                        context = context,
                        preferences = preferences
                    )
                    3 -> MoneyGuideScreen(
                        context = context,
                        preferences = preferences
                    )
                    4 -> HardeningScreen(
                        context = context,
                        preferences = preferences
                    )
                    5 -> LogsTabContent(
                        logs = logs,
                        onClearLogs = {
                            coroutineScope.launch { preferences.clearTriggerLogs() }
                        }
                    )
                    6 -> SettingsTabContent(
                        context = context,
                        config = triggerConfig,
                        preferences = preferences
                    )
                }
            }
        }
    }
}

@Composable
fun TriggersTabContent(
    context: Context,
    config: TriggerConfig,
    isAccessibilityActive: Boolean,
    preferences: DuressPreferences
) {
    val coroutineScope = rememberCoroutineScope()
    var showRestrictedSettingsGuide by remember { mutableStateOf(!isAccessibilityActive) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Accessibility Service Status Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isAccessibilityActive) SafeGreen.copy(alpha = 0.15f) else UrgentRed.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (isAccessibilityActive) SafeGreen else UrgentRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(
                                    if (isAccessibilityActive) R.string.status_service_active else R.string.status_service_inactive
                                ),
                                fontWeight = FontWeight.Bold,
                                color = if (isAccessibilityActive) SafeGreen else UrgentRed
                            )
                        }

                        if (!isAccessibilityActive) {
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        })
                                    } catch (_: Exception) {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                            ) {
                                Text(stringResource(R.string.permission_accessibility_btn))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.permission_accessibility_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Android 13+ Restricted Settings Guide for Sideloaded APKs
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = WarningAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.restricted_settings_title),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(onClick = { showRestrictedSettingsGuide = !showRestrictedSettingsGuide }) {
                            Icon(
                                if (showRestrictedSettingsGuide) Icons.Default.Visibility else Icons.Default.Info,
                                contentDescription = null
                            )
                        }
                    }

                    AnimatedVisibility(visible = showRestrictedSettingsGuide) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = stringResource(R.string.restricted_settings_summary),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.restricted_settings_step_title),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = stringResource(R.string.restricted_step_1), style = MaterialTheme.typography.bodySmall)
                            Text(text = stringResource(R.string.restricted_step_2), style = MaterialTheme.typography.bodySmall)
                            Text(text = stringResource(R.string.restricted_step_3), style = MaterialTheme.typography.bodySmall)
                            Text(text = stringResource(R.string.restricted_step_4), style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.open_app_info_btn))
                            }
                        }
                    }
                }
            }
        }

        // 3. Hardware Key Sequence Trigger Config
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.section_key_sequence),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = stringResource(R.string.key_sequence_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = stringResource(R.string.trigger_key_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = config.triggerKeyCode == KeyEvent.KEYCODE_VOLUME_DOWN,
                            onClick = {
                                coroutineScope.launch {
                                    preferences.updateTriggerKey(KeyEvent.KEYCODE_VOLUME_DOWN)
                                }
                            },
                            label = { Text(stringResource(R.string.trigger_key_vol_down)) }
                        )
                        FilterChip(
                            selected = config.triggerKeyCode == KeyEvent.KEYCODE_VOLUME_UP,
                            onClick = {
                                coroutineScope.launch {
                                    preferences.updateTriggerKey(KeyEvent.KEYCODE_VOLUME_UP)
                                }
                            },
                            label = { Text(stringResource(R.string.trigger_key_vol_up)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.sequence_count_label, config.sequenceCount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = config.sequenceCount.toFloat(),
                        onValueChange = {
                            coroutineScope.launch { preferences.updateSequenceCount(it.toInt()) }
                        },
                        valueRange = 3f..6f,
                        steps = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.time_window_label, config.windowDurationMs / 1000f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = config.windowDurationMs / 1000f,
                        onValueChange = {
                            coroutineScope.launch { preferences.updateWindowDuration((it * 1000).toLong()) }
                        },
                        valueRange = 1.5f..5.0f,
                        steps = 6
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.debounce_label, config.debounceMs),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.debounce_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = config.debounceMs.toFloat(),
                        onValueChange = {
                            coroutineScope.launch { preferences.updateDebounce(it.toLong()) }
                        },
                        valueRange = 50f..300f,
                        steps = 4
                    )
                }
            }
        }

        // 4. Quick Settings Tile & Widget Info
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quick Settings Tile & Widget Triggers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Quick Settings: Swipe down from top of screen twice, tap the Edit (pencil) button, and drag \"Duress SOS\" into your active tiles.\n\n• Home Screen Widget: Long-press any empty space on your home screen, tap Widgets, find \"Duress\", and add the 1x1 SOS button.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 5. Trigger Pipeline Simulator Button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.section_test),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            TriggerManager.onTrigger(
                                context = context,
                                source = TriggerSource.TEST_BUTTON,
                                details = "Triggered via in-app test button"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.test_trigger_btn),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PipelineTabContent(
    context: Context,
    pipelineConfig: PipelineConfig,
    preferences: DuressPreferences
) {
    val coroutineScope = rememberCoroutineScope()

    var showSmsExplanation by remember { mutableStateOf(false) }
    var showLocationExplanation by remember { mutableStateOf(false) }
    var showDndExplanation by remember { mutableStateOf(false) }
    var showNotificationExplanation by remember { mutableStateOf(false) }

    var newContactNumber by remember { mutableStateOf("") }
    var testPhoneNumberInput by remember { mutableStateOf(pipelineConfig.testPhoneNumber) }

    val hasSms = SmsSender.hasSmsPermission(context)
    val hasLocation = LocationHelper.hasLocationPermission(context)
    val hasDnd = DndHelper.isDndAccessGranted(context)
    val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    // Explanation Dialogs
    if (showSmsExplanation) {
        AlertDialog(
            onDismissRequest = { showSmsExplanation = false },
            title = { Text(stringResource(R.string.perm_sms_explanation_title)) },
            text = { Text(stringResource(R.string.perm_sms_explanation_body)) },
            confirmButton = {
                Button(onClick = {
                    showSmsExplanation = false
                    smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                }) {
                    Text(stringResource(R.string.grant_permission_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmsExplanation = false }) {
                    Text(stringResource(R.string.dismiss_btn))
                }
            }
        )
    }

    if (showLocationExplanation) {
        AlertDialog(
            onDismissRequest = { showLocationExplanation = false },
            title = { Text(stringResource(R.string.perm_location_explanation_title)) },
            text = { Text(stringResource(R.string.perm_location_explanation_body)) },
            confirmButton = {
                Button(onClick = {
                    showLocationExplanation = false
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }) {
                    Text(stringResource(R.string.grant_permission_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationExplanation = false }) {
                    Text(stringResource(R.string.dismiss_btn))
                }
            }
        )
    }

    if (showDndExplanation) {
        AlertDialog(
            onDismissRequest = { showDndExplanation = false },
            title = { Text(stringResource(R.string.perm_dnd_explanation_title)) },
            text = { Text(stringResource(R.string.perm_dnd_explanation_body)) },
            confirmButton = {
                Button(onClick = {
                    showDndExplanation = false
                    try {
                        context.startActivity(DndHelper.getDndSettingsIntent())
                    } catch (_: Exception) {}
                }) {
                    Text(stringResource(R.string.grant_permission_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDndExplanation = false }) {
                    Text(stringResource(R.string.dismiss_btn))
                }
            }
        )
    }

    if (showNotificationExplanation) {
        AlertDialog(
            onDismissRequest = { showNotificationExplanation = false },
            title = { Text(stringResource(R.string.perm_notification_explanation_title)) },
            text = { Text(stringResource(R.string.perm_notification_explanation_body)) },
            confirmButton = {
                Button(onClick = {
                    showNotificationExplanation = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) {
                    Text(stringResource(R.string.grant_permission_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationExplanation = false }) {
                    Text(stringResource(R.string.dismiss_btn))
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Header
        item {
            Text(
                text = stringResource(R.string.section_pipeline),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = stringResource(R.string.pipeline_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Safety Test Mode Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (pipelineConfig.isTestMode) WarningAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.test_mode_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = stringResource(R.string.test_mode_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = pipelineConfig.isTestMode,
                            onCheckedChange = {
                                coroutineScope.launch { preferences.setIsTestMode(it) }
                            }
                        )
                    }

                    if (pipelineConfig.isTestMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.test_phone_number_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = testPhoneNumberInput,
                                onValueChange = {
                                    testPhoneNumberInput = it
                                    coroutineScope.launch { preferences.setTestPhoneNumber(it) }
                                },
                                placeholder = { Text(stringResource(R.string.test_phone_number_hint)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // 2. Action: Silent SMS SOS & Emergency Contacts
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.action_sms_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = stringResource(R.string.action_sms_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = pipelineConfig.actionSmsEnabled,
                            onCheckedChange = {
                                coroutineScope.launch { preferences.setActionSmsEnabled(it) }
                            }
                        )
                    }

                    if (!hasSms) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showSmsExplanation = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant SMS Permission (Required for SOS)")
                        }
                    }

                    if (pipelineConfig.actionSmsEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Text(
                            text = stringResource(R.string.emergency_contacts_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = newContactNumber,
                                onValueChange = { newContactNumber = it },
                                placeholder = { Text(stringResource(R.string.emergency_contacts_hint)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newContactNumber.isNotBlank()) {
                                        coroutineScope.launch {
                                            preferences.addEmergencyContact(newContactNumber)
                                            newContactNumber = ""
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }

                        if (pipelineConfig.emergencyContacts.isEmpty() && !pipelineConfig.isTestMode) {
                            Text(
                                text = stringResource(R.string.no_contacts_warning),
                                color = WarningAmber,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            pipelineConfig.emergencyContacts.forEach { contact ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = contact, fontWeight = FontWeight.SemiBold)
                                    IconButton(onClick = {
                                        coroutineScope.launch { preferences.removeEmergencyContact(contact) }
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = UrgentRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Action: Live Location Tracking (15 min refresh 60s)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.action_location_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = stringResource(R.string.action_location_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = pipelineConfig.actionLocationTrackingEnabled,
                            onCheckedChange = {
                                coroutineScope.launch { preferences.setActionLocationTrackingEnabled(it) }
                            }
                        )
                    }

                    if (!hasLocation) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showLocationExplanation = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant Location Permission (GPS Maps Link)")
                        }
                    }

                    if (!hasNotification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showNotificationExplanation = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Allow Background Notification (Keeps 15m alive)")
                        }
                    }
                }
            }
        }

        // 4. Action: Instant Device Lock
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.action_lock_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = stringResource(R.string.action_lock_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = pipelineConfig.actionLockEnabled,
                            onCheckedChange = {
                                coroutineScope.launch { preferences.setActionLockEnabled(it) }
                            }
                        )
                    }
                }
            }
        }

        // 5. Action: Silence Phone (Do Not Disturb)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.action_dnd_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = stringResource(R.string.action_dnd_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = pipelineConfig.actionDndEnabled,
                            onCheckedChange = {
                                coroutineScope.launch { preferences.setActionDndEnabled(it) }
                            }
                        )
                    }

                    if (!hasDnd) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showDndExplanation = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.dnd_permission_btn))
                        }
                    }
                }
            }
        }

        // 6. Action: Wipe Duress Logs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = UrgentRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.action_clear_logs_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = stringResource(R.string.action_clear_logs_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = pipelineConfig.actionClearLogsEnabled,
                        onCheckedChange = {
                            coroutineScope.launch { preferences.setActionClearLogsEnabled(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LogsTabContent(
    logs: List<TriggerLogEntry>,
    onClearLogs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.logs_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = stringResource(R.string.logs_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (logs.isNotEmpty()) {
                IconButton(onClick = onClearLogs) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.clear_logs_btn))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.logs_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(logs, key = { it.id }) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (log.source) {
                                    TriggerSource.KEY_SEQUENCE -> Icons.AutoMirrored.Filled.VolumeDown
                                    TriggerSource.QS_TILE -> Icons.Default.TouchApp
                                    TriggerSource.WIDGET -> Icons.Default.Widgets
                                    TriggerSource.DECOY_PIN -> Icons.Default.Lock
                                    TriggerSource.TEST_BUTTON -> Icons.Default.Security
                                    TriggerSource.SYSTEM_BOOT -> Icons.Default.BatteryChargingFull
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.source.displayNameEn,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                if (log.details.isNotBlank()) {
                                    Text(
                                        text = log.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = log.formattedTime(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTabContent(
    context: Context,
    config: TriggerConfig,
    preferences: DuressPreferences
) {
    val coroutineScope = rememberCoroutineScope()
    val isDeviceAdminActive = DuressDeviceAdminReceiver.isActive(context)
    val securityConfig by preferences.securityConfigFlow.collectAsState(initial = SecurityConfig())

    var realPinInput by remember { mutableStateOf("") }
    var duressPinInput by remember { mutableStateOf("") }
    var pinErrorMessage by remember { mutableStateOf<String?>(null) }
    var pinSuccessMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. PIN & Decoy Security Card (Phase 3)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.section_pin_security),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = stringResource(R.string.pin_security_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = securityConfig.pinProtectionEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    preferences.setPinProtectionEnabled(enabled)
                                }
                            }
                        )
                    }

                    if (securityConfig.pinProtectionEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Real PIN Input
                        Text(
                            text = stringResource(R.string.real_pin_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = realPinInput,
                            onValueChange = {
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    realPinInput = it
                                    pinErrorMessage = null
                                    pinSuccessMessage = null
                                }
                            },
                            placeholder = { Text("4–6 digits") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Duress PIN Input
                        Text(
                            text = stringResource(R.string.duress_pin_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = duressPinInput,
                            onValueChange = {
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    duressPinInput = it
                                    pinErrorMessage = null
                                    pinSuccessMessage = null
                                }
                            },
                            placeholder = { Text("4–6 digits (must differ from Real PIN)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Decoy Theme Selector
                        Text(
                            text = stringResource(R.string.decoy_theme_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = securityConfig.decoyTheme == "NOTES",
                                onClick = {
                                    coroutineScope.launch { preferences.setDecoyTheme("NOTES") }
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.decoy_theme_notes), style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = securityConfig.decoyTheme == "CLEAN",
                                onClick = {
                                    coroutineScope.launch { preferences.setDecoyTheme("CLEAN") }
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.decoy_theme_clean), style = MaterialTheme.typography.bodyMedium)
                        }

                        if (pinErrorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pinErrorMessage!!,
                                color = UrgentRed,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (pinSuccessMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pinSuccessMessage!!,
                                color = SafeGreen,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (realPinInput.length < 4 || duressPinInput.length < 4) {
                                    pinErrorMessage = context.getString(R.string.pin_error_short)
                                    pinSuccessMessage = null
                                } else if (realPinInput == duressPinInput) {
                                    pinErrorMessage = context.getString(R.string.pin_error_same)
                                    pinSuccessMessage = null
                                } else {
                                    coroutineScope.launch {
                                        val success = preferences.configurePins(realPinInput, duressPinInput)
                                        if (success) {
                                            pinSuccessMessage = context.getString(R.string.pin_saved_success)
                                            pinErrorMessage = null
                                            realPinInput = ""
                                            duressPinInput = ""
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.pin_save_btn))
                        }
                    }
                }
            }
        }

        // 2. Haptics Settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.haptic_confirm_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = stringResource(R.string.haptic_confirm_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = config.hapticEnabled,
                            onCheckedChange = {
                                coroutineScope.launch { preferences.setHapticEnabled(it) }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { HapticFeedbackHelper.performConfirmHaptic(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.test_haptics_button))
                    }
                }
            }
        }

        // 3. Stealth Disguise Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.section_stealth),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = stringResource(R.string.stealth_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.stealth_select_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val isUrdu = config.appLanguage == "ur"
                    StealthIconManager.Disguise.entries.forEach { disguise ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = config.stealthDisguise == disguise.id,
                                onClick = {
                                    coroutineScope.launch {
                                        preferences.setStealthDisguise(disguise.id)
                                        StealthIconManager.applyDisguise(context, disguise)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUrdu) disguise.labelUr else disguise.labelEn,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // 4. Device Admin Card (Fallback for Android 8.0-8.1 or when accessibility lock unavailable)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.device_admin_label),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.device_admin_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isDeviceAdminActive) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.device_admin_active),
                                color = SafeGreen,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(DuressDeviceAdminReceiver.getActivationIntent(context))
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.device_admin_btn))
                        }
                    }
                }
            }
        }
    }
}
