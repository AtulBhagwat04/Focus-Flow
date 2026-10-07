package com.focusflow.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.feature.stats.components.ScreenTimeSummaryCard
import com.focusflow.feature.stats.components.TimeRangeSelector
import com.focusflow.feature.stats.components.TopAppsCard

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
    onNavigateToPermissions: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatsScreenContent(
        uiState = uiState,
        onSelectRange = viewModel::onSelectRange,
        onRefresh = viewModel::refresh,
        onNavigateToPermissions = onNavigateToPermissions,
        modifier = modifier,
    )
}

@Composable
fun StatsScreenContent(
    uiState: StatsUiState,
    onSelectRange: (StatsTimeRange) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(
                text = "Digital Wellbeing",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your daily screen time and focus sessions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (!uiState.hasUsagePermission) {
            PermissionRequiredCard(onGrantClicked = onNavigateToPermissions)
        }

        TimeRangeSelector(
            selectedRange = uiState.selectedRange,
            onRangeSelected = onSelectRange,
        )

        ScreenTimeSummaryCard(
            screenTime = uiState.totalScreenTimeFormatted,
            focusTime = uiState.totalFocusTimeFormatted,
            focusRatio = uiState.focusRatio,
            unlockCount = uiState.unlockCount,
        )

        TopAppsCard(apps = uiState.topApps)
    }
}

@Composable
private fun PermissionRequiredCard(
    onGrantClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Usage Access Required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "To show accurate screen time and top apps, FocusFlow needs Usage Access permission.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onGrantClicked) {
                Text("Grant in Settings")
            }
        }
    }
}
