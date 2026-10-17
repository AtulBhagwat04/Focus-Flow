package com.focusflow.feature.limits

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.component.FocusFlowActionButton
import com.focusflow.core.designsystem.component.FocusFlowAppBar
import com.focusflow.core.designsystem.component.FocusFlowBackButton
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusForestMintBorder
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.feature.limits.components.AccessibilityDisclosureDialog
import com.focusflow.feature.limits.components.AddStudyChannelDialog
import com.focusflow.feature.limits.components.AdvancedFeedBlockingCard
import com.focusflow.feature.limits.components.AppLimitListItem
import com.focusflow.feature.limits.components.EditLimitDialog
import com.focusflow.feature.limits.components.StudyModeCard

private val ScreenBackground = Color(0xFFF7FAF8)
private val TabBarBackground = Color(0xFFEFF4F1)

@Composable
fun LimitsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: LimitsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var isSearchActive by remember { mutableStateOf(false) }
    var editingApp by remember { mutableStateOf<AppLimitUiItem?>(null) }
    var showDisclosureDialog by remember { mutableStateOf(false) }
    var showAddChannelDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (isSearchActive || uiState.searchQuery.isNotEmpty()) {
            isSearchActive = false
            viewModel.onSearchQueryChanged("")
        } else {
            onBack()
        }
    }

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
            LimitsTopBar(
                isSearchActive = isSearchActive,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChanged,
                onSearchToggled = { active ->
                    isSearchActive = active
                    if (!active) viewModel.onSearchQueryChanged("")
                },
                onBack = onBack,
            )
        },
        modifier = modifier.fillMaxSize(),
        containerColor = ScreenBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            SummaryMetricsRow(
                totalCount = uniqueApps.size,
                blockedCount = blockedCount,
                safeCount = safeCount,
            )

            Spacer(modifier = Modifier.height(14.dp))

            RefinedSegmentedTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                BlockAndLimitTab(
                    apps = uniqueApps,
                    searchQuery = uiState.searchQuery,
                    onToggleBlock = viewModel::onToggleBlockDuringFocus,
                    onEditLimits = { editingApp = it },
                )
            } else {
                AdvanceBlockingTab(
                    uiState = uiState,
                    viewModel = viewModel,
                    onRequestAccessibility = { showDisclosureDialog = true },
                    onAddChannelClicked = { showAddChannelDialog = true },
                )
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
private fun LimitsTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggled: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    if (isSearchActive || searchQuery.isNotEmpty()) {
        LimitsSearchAppBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onCloseSearch = { onSearchToggled(false) },
        )
    } else {
        FocusFlowAppBar(
            title = "App Blocking & Limits",
            subtitle = "Distraction control & screen limits",
            onBack = onBack,
            containerColor = ScreenBackground,
            actions = {
                FocusFlowActionButton(
                    icon = Icons.Default.Search,
                    contentDescription = "Search installed apps",
                    onClick = { onSearchToggled(true) },
                )
            },
        )
    }
}

@Composable
private fun LimitsSearchAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ScreenBackground,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FocusFlowBackButton(onClick = onCloseSearch, contentDescription = "Close search")

            Spacer(modifier = Modifier.width(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search installed apps...",
                        color = FocusMutedText.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
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
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
            )
        }
    }
}

@Composable
private fun SummaryMetricsRow(
    totalCount: Int,
    blockedCount: Int,
    safeCount: Int,
) {
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
                count = totalCount.toString(),
                label = "Apps Found",
                color = FocusDarkText,
            )

            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(FocusCardBorder),
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
                    .background(FocusCardBorder),
            )

            SummaryMetric(
                count = safeCount.toString(),
                label = "Safe Listed",
                color = FocusForestGreenDeep,
            )
        }
    }
}

@Composable
private fun RefinedSegmentedTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = TabBarBackground,
        border = BorderStroke(1.dp, FocusCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
        ) {
            PillTabItem(
                selected = selectedTab == 0,
                title = "Block & Limit",
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f),
            )

            PillTabItem(
                selected = selectedTab == 1,
                title = "Advance Blocking",
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BlockAndLimitTab(
    apps: List<AppLimitUiItem>,
    searchQuery: String,
    onToggleBlock: (String, Boolean) -> Unit,
    onEditLimits: (AppLimitUiItem) -> Unit,
) {
    if (apps.isEmpty()) {
        EmptyAppsState(searchQuery = searchQuery)
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                items = apps,
                key = { it.packageName },
            ) { item ->
                AppLimitListItem(
                    item = item,
                    onToggleBlock = { blocked ->
                        onToggleBlock(item.packageName, blocked)
                    },
                    onSetLimitClicked = { onEditLimits(item) },
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EmptyAppsState(searchQuery: String) {
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
                text = if (searchQuery.isNotBlank()) "No matching apps found" else "Loading installed apps...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FocusDarkText,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (searchQuery.isNotBlank()) {
                    "Try checking the spelling or search with a different keyword."
                } else {
                    "Retrieving launcher applications on this device."
                },
                style = MaterialTheme.typography.bodySmall,
                color = FocusMutedText,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AdvanceBlockingTab(
    uiState: LimitsUiState,
    viewModel: LimitsViewModel,
    onRequestAccessibility: () -> Unit,
    onAddChannelClicked: () -> Unit,
) {
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
            onRequestAccessibility = onRequestAccessibility,
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
            onAddChannelClicked = onAddChannelClicked,
            onRemoveChannelClicked = viewModel::onRemoveStudyChannel,
        )

        Spacer(modifier = Modifier.height(24.dp))
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
        shape = RoundedCornerShape(12.dp),
        color = tabBackground,
        shadowElevation = if (selected) 1.5.dp else 0.dp,
        border = if (selected) BorderStroke(0.5.dp, FocusCardBorder) else null,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
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
                fontSize = 13.5.sp,
            )
        }
    }
}
