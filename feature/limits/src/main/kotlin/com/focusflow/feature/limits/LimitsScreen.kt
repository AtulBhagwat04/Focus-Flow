package com.focusflow.feature.limits

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.component.FocusFlowAppBar
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusForestMintBorder
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.feature.limits.components.AccessibilityDisclosureDialog
import com.focusflow.feature.limits.components.AddStudyChannelDialog
import com.focusflow.feature.limits.components.AdvancedFeedBlockingCard
import com.focusflow.feature.limits.components.AppLimitItemCard
import com.focusflow.feature.limits.components.EditLimitDialog
import com.focusflow.feature.limits.components.StudyModeCard

@Composable
fun LimitsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: LimitsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    var editingApp by remember { mutableStateOf<AppLimitUiItem?>(null) }
    var showDisclosureDialog by remember { mutableStateOf(false) }
    var showAddChannelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onClearMessage()
        }
    }

    val uniqueApps = remember(uiState.apps) { uiState.apps.distinctBy { it.packageName } }
    val blockedCount = remember(uniqueApps) {
        uniqueApps.count { !it.isSafeListed && it.blockDuringFocus }
    }
    val safeCount = remember(uniqueApps) {
        uniqueApps.count { it.isSafeListed }
    }

    Scaffold(
        topBar = {
            FocusFlowAppBar(
                title = "App Blocking & Limits",
                subtitle = "Distraction control & screen limits",
                onBack = onBack,
                containerColor = Color(0xFFF7FAF8),
            )
        },
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF7FAF8),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // ── Quick Summary Cards Row ───────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, FocusCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SummaryMetric(
                        count = uniqueApps.size.toString(),
                        label = "Apps Found",
                        color = FocusDarkText,
                    )

                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp)
                            .background(FocusCardBorder)
                    )

                    SummaryMetric(
                        count = blockedCount.toString(),
                        label = "Blocked in Focus",
                        color = FocusForestGreen,
                    )

                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp)
                            .background(FocusCardBorder)
                    )

                    SummaryMetric(
                        count = safeCount.toString(),
                        label = "Safe Listed",
                        color = FocusForestGreenDeep,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Segmented Pill Tabs Selector ──────────────────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFEBF1EE),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                ) {
                    // Tab 0: App Limits
                    PillTabItem(
                        selected = selectedTab == 0,
                        title = "App Limits (${uniqueApps.size})",
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f),
                    )

                    // Tab 1: Advanced & Feeds
                    PillTabItem(
                        selected = selectedTab == 1,
                        title = "Feeds & Study",
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Tab Content ───────────────────────────────────────────────────
            if (selectedTab == 0) {
                // Search Field
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = "Search installed apps...",
                            color = FocusMutedText.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = FocusForestGreen,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = FocusMutedText,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusForestMintBorder,
                        unfocusedBorderColor = FocusCardBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = FocusForestGreen,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (uniqueApps.isEmpty()) {
                    // Empty Search Results State
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(FocusForestMintBg),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Spa,
                                    contentDescription = null,
                                    tint = FocusForestGreen,
                                    modifier = Modifier.size(32.dp),
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) "No matching apps found" else "Loading installed apps...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FocusDarkText,
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) "Try checking the spelling or search with a different keyword." else "Retrieving launcher applications on this device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = FocusMutedText,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    // App List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = uniqueApps,
                            key = { it.packageName },
                        ) { item ->
                            AppLimitItemCard(
                                item = item,
                                onToggleBlockDuringFocus = { enabled ->
                                    viewModel.onToggleBlockDuringFocus(item.packageName, enabled)
                                },
                                onEditLimitsClicked = { editingApp = item },
                                onToggleEssential = { isEssential ->
                                    viewModel.onToggleEssential(item.packageName, isEssential)
                                },
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            } else {
                // Tab 1: Advanced Shorts/Reels & Study Mode
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AdvancedFeedBlockingCard(
                        config = uiState.advancedBlockingConfig,
                        isAccessibilityEnabled = uiState.isAccessibilityEnabled,
                        onRequestAccessibility = { showDisclosureDialog = true },
                        onToggleShorts = viewModel::onToggleShorts,
                        onToggleReels = viewModel::onToggleReels,
                        onToggleFocusOnly = viewModel::onToggleFocusOnly,
                    )

                    StudyModeCard(
                        config = uiState.studyModeConfig,
                        channels = uiState.studyChannels,
                        isAccessibilityEnabled = uiState.isAccessibilityEnabled,
                        onToggleStudyMode = viewModel::onToggleStudyMode,
                        onToggleFailClosed = viewModel::onToggleFailClosed,
                        onToggleChannel = viewModel::onToggleChannel,
                        onAddChannelClicked = { showAddChannelDialog = true },
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showDisclosureDialog) {
        AccessibilityDisclosureDialog(
            onConfirm = {
                showDisclosureDialog = false
                try {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } catch (_: Exception) {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            },
            onDismiss = { showDisclosureDialog = false },
        )
    }

    if (showAddChannelDialog) {
        AddStudyChannelDialog(
            onDismiss = { showAddChannelDialog = false },
            onConfirm = { channelTitle ->
                showAddChannelDialog = false
                viewModel.onAddStudyChannel(channelTitle)
            },
        )
    }

    editingApp?.let { app ->
        EditLimitDialog(
            item = app,
            onDismiss = { editingApp = null },
            onConfirm = { minutes, launchLimit ->
                viewModel.onSetDailyLimit(app.packageName, minutes, launchLimit)
                editingApp = null
            },
        )
    }
}

@Composable
private fun SummaryMetric(
    count: String,
    label: String,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 17.sp,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = FocusMutedText,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun PillTabItem(
    selected: Boolean,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabBackground by animateColorAsState(
        targetValue = if (selected) Color.White else Color.Transparent,
        label = "tab_bg",
    )
    val tabTextColor by animateColorAsState(
        targetValue = if (selected) FocusForestGreenDeep else FocusMutedText,
        label = "tab_text",
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tabBackground,
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = tabTextColor,
                fontSize = 13.sp,
            )
        }
    }
}
