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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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

import androidx.compose.ui.text.font.FontFamily

private val ScreenBackground = Color(0xFFF3F7F4)
private val TabBarBackground = Color(0xFFE5EDE8)

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
            Spacer(modifier = Modifier.height(8.dp))

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
            onBack = onBack,
            containerColor = ScreenBackground,
            titleContent = {
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "App Blocking & Limits",
                        fontFamily = FontFamily.Serif,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14291E),
                        letterSpacing = (-0.3).sp,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Distraction control & screen limits",
                        fontSize = 12.5.sp,
                        color = FocusMutedText,
                        maxLines = 1,
                    )
                }
            },
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FocusFlowBackButton(
                    onClick = onCloseSearch,
                    contentDescription = "Close search",
                )

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(21.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFD6E2DB)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = FocusForestGreen,
                            modifier = Modifier.size(18.dp),
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF14291E),
                            ),
                            cursorBrush = SolidColor(FocusForestGreen),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Search,
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    if (query.isEmpty()) {
                                        Text(
                                            text = "Search installed apps...",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = FocusMutedText.copy(alpha = 0.7f),
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )

                        if (query.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5EDE8))
                                    .clickable { onQueryChange("") },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = FocusForestGreenDeep,
                                    modifier = Modifier.size(13.dp),
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
private fun SummaryMetricsRow(
    totalCount: Int,
    blockedCount: Int,
    safeCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2EBE5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryMetric(
                count = totalCount.toString(),
                label = "Apps found",
                color = Color(0xFF14291E),
            )

            Box(
                modifier = Modifier
                    .height(32.dp)
                    .width(1.dp)
                    .background(Color(0xFFE2EBE5)),
            )

            SummaryMetric(
                count = blockedCount.toString(),
                label = "Blocked in focus",
                color = Color(0xFF2E6B4D),
            )

            Box(
                modifier = Modifier
                    .height(32.dp)
                    .width(1.dp)
                    .background(Color(0xFFE2EBE5)),
            )

            SummaryMetric(
                count = safeCount.toString(),
                label = "Safe listed",
                color = Color(0xFF14291E),
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
        shape = RoundedCornerShape(26.dp),
        color = TabBarBackground,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
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
                title = "Advanced Blocking",
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = FocusDarkText,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (searchQuery.isNotBlank()) {
                    "Try checking the spelling or search with a different keyword."
                } else {
                    "Retrieving launcher applications on this device."
                },
                fontSize = 13.sp,
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
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 24.sp,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = FocusMutedText,
            fontWeight = FontWeight.Normal,
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
        targetValue = if (selected) FocusForestGreenDeep else Color(0xFF5A6F62),
        label = "tab_text",
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = tabBackground,
        shadowElevation = if (selected) 2.dp else 0.dp,
        border = if (selected) BorderStroke(0.5.dp, Color(0xFFD6E2DB)) else null,
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = tabTextColor,
                fontSize = 13.5.sp,
            )
        }
    }
}
