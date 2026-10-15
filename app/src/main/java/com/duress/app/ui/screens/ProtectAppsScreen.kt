package com.duress.app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duress.app.R
import com.duress.app.data.DuressPreferences
import com.duress.app.protection.AppChecklistItem
import com.duress.app.protection.AppProtectionData
import com.duress.app.protection.AppProtectionHelper
import com.duress.app.protection.ChecklistActionType
import com.duress.app.protection.ChecklistPriority
import com.duress.app.protection.OemBrand
import com.duress.app.protection.OemGuideItem
import com.duress.app.ui.theme.SafeGreen
import com.duress.app.ui.theme.UrgentRed
import com.duress.app.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@Composable
fun ProtectAppsScreen(
    context: Context,
    preferences: DuressPreferences
) {
    val coroutineScope = rememberCoroutineScope()
    val protectedSet by preferences.protectedAppsFlow.collectAsState(initial = emptySet())

    val detectedBrand = remember { AppProtectionHelper.detectOemBrand() }
    val checklistItems = remember { AppProtectionData.checklistItems }
    val oemGuides = remember { AppProtectionData.getOemGuides() }

    var selectedSubTab by remember { mutableIntStateOf(0) }
    val expandedStepsMap = remember { mutableStateMapOf<String, Boolean>() }

    val totalItems = checklistItems.size
    val securedCount = checklistItems.count { protectedSet.contains(it.id) }
    val progressFraction = if (totalItems > 0) securedCount.toFloat() / totalItems.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "scoreProgress")
    val progressPercent = (progressFraction * 100).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Detected OEM Device Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.detected_device_title, detectedBrand.displayName),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = when (detectedBrand) {
                                    OemBrand.TRANSSION -> "XOS / HiOS XHide & App Freezer supported"
                                    OemBrand.SAMSUNG -> "Samsung Knox & Secure Folder supported"
                                    OemBrand.XIAOMI -> "MIUI / HyperOS Second Space & App Lock supported"
                                    OemBrand.VIVO -> "FuntouchOS Privacy App Encryption supported"
                                    OemBrand.OPPO -> "ColorOS Hide Apps & Private Safe supported"
                                    OemBrand.GOOGLE -> "Android 15 Private Space supported"
                                    OemBrand.GENERIC -> "Universal Android Work Profile (Shelter/Island) supported"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            when (detectedBrand) {
                                OemBrand.TRANSSION -> AppProtectionHelper.openTranssionXHide(context)
                                OemBrand.SAMSUNG -> AppProtectionHelper.openSamsungSecureFolder(context)
                                OemBrand.XIAOMI -> AppProtectionHelper.openXiaomiSecondSpace(context)
                                OemBrand.VIVO -> AppProtectionHelper.openVivoAppHide(context)
                                OemBrand.OPPO -> AppProtectionHelper.openOppoPrivateSafe(context)
                                OemBrand.GOOGLE -> AppProtectionHelper.openPrivateSpaceSettings(context)
                                OemBrand.GENERIC -> AppProtectionHelper.openShelterOrIsland(context)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.detected_device_action),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 2. Rule 5 Compliance Disclaimer Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.protect_disclaimer_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.protect_disclaimer_body),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 3. App Hardening Progress Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.security_score_label),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = stringResource(
                                    R.string.security_score_status,
                                    securedCount,
                                    totalItems,
                                    progressPercent
                                ),
                                fontSize = 12.sp,
                                color = if (progressPercent >= 80) SafeGreen else if (progressPercent >= 40) WarningAmber else UrgentRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (securedCount > 0) {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch { preferences.resetProtectedApps() }
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.reset_checklist_btn), fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progressPercent >= 80) SafeGreen else if (progressPercent >= 40) WarningAmber else UrgentRed,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // 4. Sub-Navigation Tabs: Security Checklist vs OEM Guides
        item {
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = {
                        Text(
                            text = "${stringResource(R.string.subtab_checklist)} ($securedCount/$totalItems)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    },
                    icon = {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = {
                        Text(
                            text = stringResource(R.string.subtab_oem_guides),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    },
                    icon = {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }

        // 5. Content Rendering
        if (selectedSubTab == 0) {
            // Checklist Items
            items(checklistItems, key = { it.id }) { item ->
                ChecklistCard(
                    context = context,
                    item = item,
                    isSecured = protectedSet.contains(item.id),
                    isExpanded = expandedStepsMap[item.id] == true,
                    onToggleSecured = { checked ->
                        coroutineScope.launch {
                            preferences.toggleProtectedApp(item.id, checked)
                        }
                    },
                    onToggleExpand = {
                        expandedStepsMap[item.id] = !(expandedStepsMap[item.id] ?: false)
                    }
                )
            }
        } else {
            // OEM Vault Guides
            items(oemGuides, key = { it.oemBrand.name }) { guide ->
                OemGuideCard(
                    context = context,
                    guide = guide,
                    isDetectedBrand = guide.oemBrand == detectedBrand
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChecklistCard(
    context: Context,
    item: AppChecklistItem,
    isSecured: Boolean,
    isExpanded: Boolean,
    onToggleSecured: (Boolean) -> Unit,
    onToggleExpand: () -> Unit
) {
    val installedPackage = remember(item.packages) {
        if (item.packages.isNotEmpty()) AppProtectionHelper.isAnyPackageInstalled(context, item.packages) else null
    }
    val hasPackages = item.packages.isNotEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSecured) 1.5.dp else 0.5.dp,
                color = if (isSecured) SafeGreen else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSecured) SafeGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Title, Priority Tag, Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSecured,
                    onCheckedChange = onToggleSecured,
                    colors = CheckboxDefaults.colors(
                        checkedColor = SafeGreen,
                        checkmarkColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(item.titleRes),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Priority Badge
                        PriorityBadge(item.priority)

                        // Installed / System Badge
                        if (hasPackages) {
                            if (installedPackage != null) {
                                Text(
                                    text = stringResource(R.string.status_installed),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SafeGreen,
                                    modifier = Modifier
                                        .background(SafeGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.status_not_installed),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            // Description
            Text(
                text = stringResource(item.descriptionRes),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 48.dp, end = 8.dp)
            )

            // Step-by-Step Accordion
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, top = 8.dp, end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = stringResource(item.stepByStepGuideRes),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (item.actionType) {
                            ChecklistActionType.LAUNCH_APP -> {
                                if (installedPackage != null) {
                                    OutlinedButton(
                                        onClick = { AppProtectionHelper.launchAppOrDetails(context, installedPackage) }
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.btn_open_app), fontSize = 11.sp)
                                    }
                                } else if (item.packages.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { AppProtectionHelper.launchAppOrDetails(context, item.packages.first()) }
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.btn_open_app), fontSize = 11.sp)
                                    }
                                }
                            }
                            ChecklistActionType.OPEN_OEM_VAULT -> {
                                OutlinedButton(
                                    onClick = { AppProtectionHelper.openSecuritySettings(context) }
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.btn_open_settings), fontSize = 11.sp)
                                }
                            }
                            ChecklistActionType.OPEN_SECURITY_SETTINGS -> {
                                OutlinedButton(
                                    onClick = { AppProtectionHelper.openSecuritySettings(context) }
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.btn_open_settings), fontSize = 11.sp)
                                }
                            }
                            ChecklistActionType.OPEN_NOTIFICATION_SETTINGS -> {
                                OutlinedButton(
                                    onClick = { AppProtectionHelper.openNotificationSettings(context) }
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.btn_open_settings), fontSize = 11.sp)
                                }
                            }
                            ChecklistActionType.OPEN_SHELTER -> {
                                OutlinedButton(
                                    onClick = { AppProtectionHelper.openShelterOrIsland(context) }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.guide_shelter_btn), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OemGuideCard(
    context: Context,
    guide: OemGuideItem,
    isDetectedBrand: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isDetectedBrand) 2.dp else 0.5.dp,
                color = if (isDetectedBrand) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDetectedBrand) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(guide.titleRes),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = stringResource(guide.featureNameRes),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isDetectedBrand) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "YOUR DEVICE",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(guide.summaryRes),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = stringResource(guide.stepsRes),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { guide.onAction(context) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDetectedBrand) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(guide.actionLabelRes),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: ChecklistPriority) {
    val (labelRes, color) = when (priority) {
        ChecklistPriority.CRITICAL -> Pair(R.string.priority_critical, UrgentRed)
        ChecklistPriority.HIGH -> Pair(R.string.priority_high, WarningAmber)
        ChecklistPriority.RECOMMENDED -> Pair(R.string.priority_recommended, MaterialTheme.colorScheme.primary)
    }

    Text(
        text = stringResource(labelRes),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
