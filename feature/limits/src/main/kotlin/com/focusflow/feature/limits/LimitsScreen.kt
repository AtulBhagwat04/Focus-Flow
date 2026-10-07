package com.focusflow.feature.limits

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.feature.limits.components.AccessibilityDisclosureDialog
import com.focusflow.feature.limits.components.AddStudyChannelDialog
import com.focusflow.feature.limits.components.AdvancedFeedBlockingCard
import com.focusflow.feature.limits.components.AppLimitItemCard
import com.focusflow.feature.limits.components.EditLimitDialog
import com.focusflow.feature.limits.components.StudyModeCard

@Composable
fun LimitsScreen(
    modifier: Modifier = Modifier,
    viewModel: LimitsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Text(
                text = "App Blocking & Study Mode",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manage app time limits, feed restrictions, and YouTube study modes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("App Limits") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Advanced & Feeds") },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Tab 0: Standard App Limits
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = { Text("Search installed apps...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = uiState.apps,
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
                }
            } else {
                // Tab 1: Advanced Shorts/Reels & Study Mode
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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
