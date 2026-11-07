package com.focusflow.feature.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusScreenBackground
import com.focusflow.core.designsystem.theme.OutfitFontFamily
import com.focusflow.feature.stats.components.FocusBarChartCard
import com.focusflow.feature.stats.components.StatsDateNavigator
import com.focusflow.feature.stats.components.StatsHeader
import com.focusflow.feature.stats.components.StatsHighlightsGrid
import com.focusflow.feature.stats.components.StatsPeriodTabs
import com.focusflow.feature.stats.components.StatsSummaryCard
import com.focusflow.feature.stats.components.TopAppsCard

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
    onNavigateToPermissions: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Automatically refresh live device usage stats whenever the user resumes this screen
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    StatsScreenContent(
        uiState = uiState,
        onSelectRange = viewModel::onSelectRange,
        onPreviousPeriod = viewModel::onPreviousPeriod,
        onNextPeriod = viewModel::onNextPeriod,
        onResetToCurrent = viewModel::onResetToCurrentPeriod,
        onNavigateToPermissions = onNavigateToPermissions,
        onRefresh = viewModel::refresh,
        modifier = modifier,
    )
}

@Composable
fun StatsScreenContent(
    uiState: StatsUiState,
    onSelectRange: (StatsTimeRange) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onResetToCurrent: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FocusScreenBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            // Top Header: "Your focus" + dynamic subtitle + Live Sync & Calendar actions
            StatsHeader(
                onCalendarClick = onResetToCurrent,
                onRefreshClick = onRefresh,
                isRefreshing = uiState.isLoading,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Period Selector Tabs (Week and Month)
            StatsPeriodTabs(
                selectedRange = uiState.selectedRange,
                onRangeSelected = onSelectRange,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Date Navigator: < Oct 19 – 25 >
            StatsDateNavigator(
                dateLabel = uiState.dateRangeLabel,
                canNavigateNext = uiState.canNavigateNext,
                onPrevious = onPreviousPeriod,
                onNext = onNextPeriod,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Permission Guidance Banner (if usage access revoked/missing)
            if (!uiState.hasUsagePermission) {
                PermissionRequiredCard(onGrantClicked = onNavigateToPermissions)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Hero Summary Card: "Focused this week", "12h 30m", Trend, Goal progress
            StatsSummaryCard(
                heroLabel = uiState.focusHeroLabel,
                totalFocusTime = uiState.totalFocusTimeFormatted,
                trendText = uiState.trendText,
                isTrendPositive = uiState.isTrendPositive,
                goalLabel = uiState.goalLabel,
                goalRemainingText = uiState.goalRemainingText,
                goalProgress = uiState.goalProgress,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Daily / Period Breakdown Bar Chart: "Focus each day", dashed goal line, Mon-Sun
            FocusBarChartCard(
                chartTitle = uiState.chartTitle,
                bars = uiState.chartBars,
                dailyGoalHours = uiState.dailyGoalHours,
                dailyGoalFormatted = uiState.dailyGoalFormatted,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2x2 Highlights Grid: Best day, Peak window, Avg length, Goal days met
            StatsHighlightsGrid(
                highlights = uiState.highlights,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Top Used Apps Breakdown Card
            TopAppsCard(apps = uiState.topApps)

            // Bottom Spacing for floating navigation bar
            Spacer(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(96.dp)
            )
        }
    }
}

@Composable
private fun PermissionRequiredCard(
    onGrantClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9F5EC),
        ),
        border = BorderStroke(1.dp, Color(0xFFECE2CE)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0E4CE),
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = Color(0xFF8A6417),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.stats_permission_required_title),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF5E420C),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.stats_permission_required_desc),
                fontFamily = OutfitFontFamily,
                fontSize = 12.5.sp,
                color = Color(0xFF6B5838),
                lineHeight = 17.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGrantClicked,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FocusForestGreenDeep,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = stringResource(R.string.stats_permission_grant_btn),
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "Reference Mockup Match", showBackground = true, widthDp = 390)
@Composable
private fun StatsScreenMockupPreview() {
    FocusFlowTheme {
        val sampleBars = listOf(
            ChartBarItem("1", "Mon", "Monday", 1.8f, "1.8h", false),
            ChartBarItem("2", "Tue", "Tuesday", 1.0f, "1h", false),
            ChartBarItem("3", "Wed", "Wednesday", 2.3f, "2.3h", true),
            ChartBarItem("4", "Thu", "Thursday", 1.6f, "1.6h", false),
            ChartBarItem("5", "Fri", "Friday", 3.0f, "3h", true, isCurrent = true),
            ChartBarItem("6", "Sat", "Saturday", 1.2f, "1.2h", false),
            ChartBarItem("7", "Sun", "Sunday", 1.6f, "1.6h", false),
        )

        StatsScreenContent(
            uiState = StatsUiState(
                selectedRange = StatsTimeRange.WEEK,
                dateRangeLabel = "Oct 19 – 25",
                canNavigateNext = false,
                focusHeroLabel = "Focused this week",
                totalFocusTimeFormatted = "12h 30m",
                subtitleText = "How you're doing this week",
                trendText = "18% more than last week. Great progress!",
                isTrendPositive = true,
                goalLabel = "Weekly goal: 16h",
                goalRemainingText = "3h 30m to go",
                goalProgress = 0.78f,
                chartTitle = "Focus each day",
                chartBars = sampleBars,
                dailyGoalHours = 2.33f,
                dailyGoalFormatted = "2h 20m",
                highlights = StatsHighlights(
                    bestDayName = "Friday",
                    bestDayCaption = "Best day · 3h",
                    bestTimeWindow = "9–11 AM",
                    bestTimeCaption = "Best time to focus",
                    avgSessionLength = "1h 20m",
                    avgSessionCaption = "Avg session length",
                    streakCount = "5 days",
                    streakCaption = "Goal days met",
                ),
                totalScreenTimeFormatted = "28h 45m",
                unlockCount = 142,
                focusRatio = 0.43f,
                topApps = listOf(
                    AppUsageItem("com.instagram", "Instagram", "4h 15m", 38, 0.15f, false),
                    AppUsageItem("com.youtube", "YouTube", "3h 50m", 25, 0.13f, false),
                    AppUsageItem("com.slack", "Slack", "2h 10m", 45, 0.08f, true),
                ),
            ),
            onSelectRange = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
            onResetToCurrent = {},
            onNavigateToPermissions = {},
        )
    }
}