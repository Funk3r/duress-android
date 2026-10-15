package com.duress.app.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
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
import com.duress.app.money.EmergencyHelpline
import com.duress.app.money.HelplineCategory
import com.duress.app.money.MoneyGuideData
import com.duress.app.money.MoneyPillarItem
import com.duress.app.ui.theme.SafeGreen
import com.duress.app.ui.theme.UrgentRed
import com.duress.app.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@Composable
fun MoneyGuideScreen(
    context: Context,
    preferences: DuressPreferences
) {
    val coroutineScope = rememberCoroutineScope()
    val completedSet by preferences.moneyGuideFlow.collectAsState(initial = emptySet())

    val pillars = remember { MoneyGuideData.pillars }
    val allHelplines = remember { MoneyGuideData.helplines }

    var selectedSubTab by remember { mutableIntStateOf(0) }
    var selectedHelplineFilter by remember { mutableStateOf<HelplineCategory?>(null) }
    val expandedPillarsMap = remember { mutableStateMapOf<String, Boolean>() }

    val totalPillars = pillars.size
    val completedCount = pillars.count { completedSet.contains(it.id) }
    val progressFraction = if (totalPillars > 0) completedCount.toFloat() / totalPillars.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "moneyProgress")
    val progressPercent = (progressFraction * 100).toInt()

    val filteredHelplines = remember(selectedHelplineFilter) {
        if (selectedHelplineFilter == null) {
            allHelplines
        } else {
            allHelplines.filter { it.category == selectedHelplineFilter }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner & OpSec Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(UrgentRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.money_guide_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.money_guide_subtitle),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Financial Hardening Progress Card
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
                                text = stringResource(R.string.money_score_label),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = stringResource(
                                    R.string.money_score_status,
                                    completedCount,
                                    totalPillars,
                                    progressPercent
                                ),
                                fontSize = 12.sp,
                                color = if (progressPercent >= 80) SafeGreen else if (progressPercent >= 40) WarningAmber else UrgentRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (completedCount > 0) {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch { preferences.resetMoneyGuide() }
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.reset_money_checklist_btn), fontSize = 11.sp)
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

        // 3. Sub-Navigation Tabs: 5 Defense Pillars vs Emergency Helplines
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
                            text = "${stringResource(R.string.subtab_pillars)} ($completedCount/$totalPillars)",
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
                            text = stringResource(R.string.subtab_helplines),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    },
                    icon = {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }

        // 4. Content Rendering
        if (selectedSubTab == 0) {
            // Defense Pillars
            items(pillars, key = { it.id }) { pillar ->
                PillarCard(
                    pillar = pillar,
                    isCompleted = completedSet.contains(pillar.id),
                    isExpanded = expandedPillarsMap[pillar.id] ?: true,
                    onToggleCompleted = { checked ->
                        coroutineScope.launch {
                            preferences.toggleMoneyGuideItem(pillar.id, checked)
                        }
                    },
                    onToggleExpand = {
                        expandedPillarsMap[pillar.id] = !(expandedPillarsMap[pillar.id] ?: true)
                    }
                )
            }
        } else {
            // Emergency Freezing Directory Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = WarningAmber.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.helpline_directory_desc),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedHelplineFilter == null,
                        onClick = { selectedHelplineFilter = null },
                        label = { Text(stringResource(R.string.filter_all), fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedHelplineFilter == HelplineCategory.TELCO_SIM,
                        onClick = { selectedHelplineFilter = HelplineCategory.TELCO_SIM },
                        label = { Text(stringResource(R.string.filter_sim), fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.SimCard, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                    FilterChip(
                        selected = selectedHelplineFilter == HelplineCategory.WALLET_EMI,
                        onClick = { selectedHelplineFilter = HelplineCategory.WALLET_EMI },
                        label = { Text(stringResource(R.string.filter_wallets), fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                    FilterChip(
                        selected = selectedHelplineFilter == HelplineCategory.COMMERCIAL_BANK,
                        onClick = { selectedHelplineFilter = HelplineCategory.COMMERCIAL_BANK },
                        label = { Text(stringResource(R.string.filter_banks), fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }

            // Helplines List
            items(filteredHelplines, key = { it.id }) { helpline ->
                HelplineCard(
                    context = context,
                    helpline = helpline
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PillarCard(
    pillar: MoneyPillarItem,
    isCompleted: Boolean,
    isExpanded: Boolean,
    onToggleCompleted: (Boolean) -> Unit,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCompleted) 1.5.dp else 0.5.dp,
                color = if (isCompleted) SafeGreen else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) SafeGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Checkbox, Title, Badge, Expand Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = onToggleCompleted,
                    colors = CheckboxDefaults.colors(
                        checkedColor = SafeGreen,
                        checkmarkColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(pillar.titleRes),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(if (pillar.isCritical) R.string.badge_critical_opsec else R.string.badge_recommended_opsec),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pillar.isCritical) UrgentRed else MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                (if (pillar.isCritical) UrgentRed else MaterialTheme.colorScheme.primary).copy(alpha = 0.15f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            // Subtitle
            Text(
                text = stringResource(pillar.subtitleRes),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 48.dp, end = 8.dp)
            )

            // Expandable Content
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, top = 10.dp, end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Threat Model Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(UrgentRed.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.why_it_matters_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = UrgentRed
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = stringResource(pillar.whyRes),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Operational Steps Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.action_steps_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = stringResource(pillar.stepsRes),
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HelplineCard(
    context: Context,
    helpline: EmergencyHelpline
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = helpline.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = helpline.displayPhone,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Category Tag
                val (tagText, tagColor) = when (helpline.category) {
                    HelplineCategory.TELCO_SIM -> Pair("SIM", WarningAmber)
                    HelplineCategory.WALLET_EMI -> Pair("WALLET", SafeGreen)
                    HelplineCategory.COMMERCIAL_BANK -> Pair("BANK", MaterialTheme.colorScheme.primary)
                }

                Text(
                    text = tagText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = tagColor,
                    modifier = Modifier
                        .background(tagColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(helpline.notesRes),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { MoneyGuideData.dialNumber(context, helpline.dialNumber) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.btn_dial_now),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
