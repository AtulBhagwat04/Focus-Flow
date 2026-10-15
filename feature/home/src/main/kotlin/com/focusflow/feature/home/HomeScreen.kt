package com.focusflow.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.component.FocusFlowActionButton
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusForestMintBorder
import com.focusflow.core.designsystem.theme.FocusMutedText


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToFocus: () -> Unit = {},
    onNavigateToLimits: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onNavigateToFocus = onNavigateToFocus,
        onNavigateToLimits = onNavigateToLimits,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToStats = onNavigateToStats,
        onNavigateToTasks = onNavigateToTasks,
        onSelectDuration = viewModel::selectDuration,
        onSelectMode = viewModel::selectMode,
        modifier = modifier,
    )
}

/**
 * Stateless content composable for Home screen.
 */
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onNavigateToFocus: () -> Unit = {},
    onNavigateToLimits: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onSelectDuration: (Int) -> Unit = {},
    onSelectMode: (FocusMode) -> Unit = {},
) {
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var showFocusModeDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // ── 1. Full Screen Scenic Nature Background ──────────────────────────
        Image(
            painter = painterResource(id = R.drawable.bg_home_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // ── 2. Soft Ambient Frost Overlay for Content Legibility ─────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = 0.20f),
                        0.22f to Color(0xFFF7FAF8).copy(alpha = 0.65f),
                        0.55f to Color(0xFFF7FAF8).copy(alpha = 0.88f),
                        1.0f to Color(0xFFF7FAF8).copy(alpha = 0.95f),
                    )
                )
        )

        // ── 3. Foreground Content ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // ── 1. Greeting + Profile Button Row ──────────────────────────
                HomeGreetingSection(
                    greeting = uiState.greeting,
                    onNavigateToProfile = onNavigateToProfile,
                    profileContentDescription = stringResource(R.string.home_cd_profile),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                )

                // ── 2. Hero Card: "Start a Focus Session" ────────────────────────
                HeroFocusCard(
                    selectedDurationMinutes = uiState.selectedDurationMinutes,
                    onSelectDuration = onSelectDuration,
                    onCustomDurationClicked = { showCustomDurationDialog = true },
                    onStartFocus = onNavigateToFocus,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── 3. Quick Actions Row ─────────────────────────────────────────
                QuickActionsSection(
                    onNavigateToLimits = onNavigateToLimits,
                    onNavigateToStats = onNavigateToStats,
                    onNavigateToTasks = onNavigateToTasks,
                    onFocusModeClicked = { showFocusModeDialog = true },
                    onSeeAllClicked = onNavigateToLimits,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── 4. Weekly Progress Card ──────────────────────────────────────
                WeeklyProgressCard(
                    focusedFormatted = uiState.weeklyFocusFormatted,
                    goalPercent = uiState.weeklyGoalPercent,
                    dailyHours = uiState.weeklyDailyHours,
                    onViewDetails = onNavigateToStats,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── 5. Inspirational Quote Banner ────────────────────────────────
                QuoteBannerCard(
                    quote = uiState.quote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                )

                // ── 6. Bottom Navigation Bar Spacing ─────────────────────────────
                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(84.dp)
                )
            }
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────
    if (showCustomDurationDialog) {
        CustomDurationDialog(
            currentMinutes = uiState.selectedDurationMinutes,
            onDismiss = { showCustomDurationDialog = false },
            onConfirm = { minutes ->
                onSelectDuration(minutes)
                showCustomDurationDialog = false
            },
        )
    }

    if (showFocusModeDialog) {
        FocusModeDialog(
            selectedMode = uiState.selectedMode,
            onSelectMode = { mode ->
                onSelectMode(mode)
                showFocusModeDialog = false
            },
            onDismiss = { showFocusModeDialog = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. Greeting Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeGreetingSection(
    greeting: String,
    onNavigateToProfile: () -> Unit,
    profileContentDescription: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        // ── Left: Greeting Text ───────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics { heading() },
            ) {
                Text(
                    text = stringResource(R.string.home_headline_lets),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = stringResource(R.string.home_headline_focus_word),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusForestGreen,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = stringResource(R.string.home_headline_today_word),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    letterSpacing = (-0.5).sp,
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = stringResource(R.string.home_headline_small_steps),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF64748B),
            )
        }

        // ── Right: Profile Action Button ──────────────────────────────────────
        Spacer(modifier = Modifier.width(12.dp))
        FocusFlowActionButton(
            icon = Icons.Outlined.Person,
            onClick = onNavigateToProfile,
            contentDescription = profileContentDescription,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Hero Focus Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroFocusCard(
    selectedDurationMinutes: Int,
    onSelectDuration: (Int) -> Unit,
    onCustomDurationClicked: () -> Unit,
    onStartFocus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startFocusActionLabel = stringResource(R.string.home_hero_start_btn)
    val minUnitLabel = stringResource(R.string.home_hero_min_label)

    Surface(
        modifier = modifier.shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color(0x10000000),
            spotColor = Color(0x1A000000),
        ),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, FocusCardBorder),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
        ) {
            // Calm background photo on the right half
            Image(
                painter = painterResource(id = R.drawable.bg_hero_focus_card),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterEnd,
                modifier = Modifier.matchParentSize(),
            )

            // Refined scrim ensuring high text legibility while keeping warm scenic atmosphere
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            0.0f to Color.White.copy(alpha = 0.96f),
                            0.45f to Color.White.copy(alpha = 0.90f),
                            0.75f to Color.White.copy(alpha = 0.68f),
                            1.0f to Color.White.copy(alpha = 0.38f),
                        ),
                    ),
            )

            // Content inside the Hero Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_hero_title),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    letterSpacing = (-0.3).sp,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.home_hero_subtitle),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF475569),
                    lineHeight = 17.sp,
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Duration Selector Pills: 25, 50, 90, Custom (uniformly distributed)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val standardDurations = listOf(25, 50, 90)
                    standardDurations.forEach { duration ->
                        val isSelected = selectedDurationMinutes == duration
                        DurationPill(
                            durationNumber = duration.toString(),
                            unitLabel = minUnitLabel,
                            isSelected = isSelected,
                            onClick = { onSelectDuration(duration) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Custom Pill
                    val isCustom = selectedDurationMinutes !in standardDurations
                    CustomPill(
                        isSelected = isCustom,
                        customMinutes = selectedDurationMinutes,
                        unitLabel = minUnitLabel,
                        onClick = onCustomDurationClicked,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // "Start Focus" CTA Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x301E5338))
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(
                            indication = ripple(bounded = true),
                            interactionSource = null,
                            onClickLabel = startFocusActionLabel,
                            onClick = onStartFocus,
                        )
                        .semantics {
                            role = Role.Button
                            contentDescription = startFocusActionLabel
                        },
                    shape = RoundedCornerShape(24.dp),
                    color = FocusForestGreenDeep,
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = startFocusActionLabel,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DurationPill(
    durationNumber: String,
    unitLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) FocusForestGreenDeep else Color.White,
        animationSpec = tween(durationMillis = 200),
        label = "pill_bg",
    )
    val numberColor = if (isSelected) Color.White else Color(0xFF0F172A)
    val unitColor = if (isSelected) Color(0xD9FFFFFF) else Color(0xFF64748B)
    val borderColor = if (isSelected) FocusForestGreenDeep else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier
            .height(54.dp)
            .shadow(
                elevation = if (isSelected) 3.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x08000000),
                spotColor = Color(0x10000000),
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                indication = ripple(bounded = true),
                interactionSource = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.RadioButton
                this.contentDescription = "$durationNumber $unitLabel"
            },
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = durationNumber,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = numberColor,
                lineHeight = 16.sp,
            )
            Text(
                text = unitLabel,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = unitColor,
                lineHeight = 12.sp,
            )
        }
    }
}

@Composable
private fun CustomPill(
    isSelected: Boolean,
    customMinutes: Int,
    unitLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) FocusForestGreenDeep else Color.White,
        animationSpec = tween(durationMillis = 200),
        label = "custom_pill_bg",
    )
    val textColor = if (isSelected) Color.White else Color(0xFF0F172A)
    val borderColor = if (isSelected) FocusForestGreenDeep else Color(0xFFE2E8F0)
    val customCd = if (isSelected) "$customMinutes $unitLabel" else stringResource(R.string.home_hero_custom)

    Surface(
        modifier = modifier
            .height(54.dp)
            .shadow(
                elevation = if (isSelected) 3.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x08000000),
                spotColor = Color(0x10000000),
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                indication = ripple(bounded = true),
                interactionSource = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.RadioButton
                this.contentDescription = customCd
            },
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        if (isSelected) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = customMinutes.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 16.sp,
                )
                Text(
                    text = unitLabel,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xD9FFFFFF),
                    lineHeight = 12.sp,
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.home_hero_custom),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Quick Actions Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsSection(
    onNavigateToLimits: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onFocusModeClicked: () -> Unit,
    onSeeAllClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_section_quick_actions),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        indication = ripple(bounded = true),
                        interactionSource = null,
                        onClick = onSeeAllClicked,
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${stringResource(R.string.home_action_see_all)} >",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4 Quick Action Cards in a responsive Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickActionItem(
                title = stringResource(R.string.home_action_block_apps),
                subtitle = stringResource(R.string.home_action_block_apps_sub),
                icon = Icons.Outlined.Block,
                iconTint = Color(0xFFDC2626),
                iconBadgeBg = Color(0xFFFEE2E2),
                onClick = onNavigateToLimits,
                modifier = Modifier.weight(1f),
            )

            QuickActionItem(
                title = stringResource(R.string.home_action_focus_stats),
                subtitle = stringResource(R.string.home_action_focus_stats_sub),
                icon = Icons.Filled.BarChart,
                iconTint = FocusForestGreenDeep,
                iconBadgeBg = Color(0xFFD1FAE5),
                onClick = onNavigateToStats,
                modifier = Modifier.weight(1f),
            )

            QuickActionItem(
                title = stringResource(R.string.home_action_tasks),
                subtitle = stringResource(R.string.home_action_tasks_sub),
                icon = Icons.AutoMirrored.Outlined.Assignment,
                iconTint = Color(0xFF2563EB),
                iconBadgeBg = Color(0xFFDBEAFE),
                onClick = onNavigateToTasks,
                modifier = Modifier.weight(1f),
            )

            QuickActionItem(
                title = stringResource(R.string.home_action_focus_mode_card),
                subtitle = stringResource(R.string.home_action_focus_mode_sub),
                icon = Icons.Outlined.Spa,
                iconTint = Color(0xFF059669),
                iconBadgeBg = Color(0xFFD1FAE5),
                onClick = onFocusModeClicked,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBadgeBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .heightIn(min = 106.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x08000000),
                spotColor = Color(0x0F000000),
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                indication = ripple(bounded = true),
                interactionSource = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Button
                this.contentDescription = "$title, $subtitle"
            },
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, FocusCardBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBadgeBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 12.sp,
                maxLines = 2,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. Weekly Progress Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WeeklyProgressCard(
    focusedFormatted: String,
    goalPercent: Int,
    dailyHours: List<Pair<String, Float>>,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.shadow(
            elevation = 3.dp,
            shape = RoundedCornerShape(22.dp),
            ambientColor = Color(0x0A000000),
            spotColor = Color(0x10000000),
        ),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, FocusCardBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            // Header: Icon + "Weekly Progress" | "View Details >"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.BarChart,
                        contentDescription = null,
                        tint = FocusForestGreen,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.home_weekly_title),
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            indication = ripple(bounded = true),
                            interactionSource = null,
                            onClick = onViewDetails,
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${stringResource(R.string.home_weekly_view_details)} >",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body: Weekly Hours & Bar Chart (Left) | Circular Gauge (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Left Column: Hours + 7-Day Bar Chart
                Column(
                    modifier = Modifier.weight(1.5f),
                ) {
                    Text(
                        text = focusedFormatted,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp,
                    )
                    Text(
                        text = stringResource(R.string.home_weekly_focused_this_week),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF64748B),
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7-day Bar chart (Mon to Sun)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        val maxDailyHour = (dailyHours.maxOfOrNull { it.second } ?: 5f).coerceAtLeast(1f)
                        dailyHours.forEach { (day, hours) ->
                            val barHeightFraction = (hours / maxDailyHour).coerceIn(0.2f, 1f)
                            val targetBarDp = (54 * barHeightFraction).dp

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(15.dp)
                                        .height(targetBarDp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 6.dp,
                                                topEnd = 6.dp,
                                                bottomStart = 4.dp,
                                                bottomEnd = 4.dp
                                            )
                                        )
                                        .background(Color(0xFF76A88E)),
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B),
                                )
                            }
                        }
                    }
                }

                // Right Column: Circular Goal Progress Ring
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val animatedSweep by animateFloatAsState(
                        targetValue = (goalPercent / 100f) * 360f,
                        animationSpec = tween(durationMillis = 800),
                        label = "progress_sweep",
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 8.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        // Background Track Ring
                        drawArc(
                            color = Color(0xFFE5ECE8),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )

                        // Foreground Progress Arc
                        drawArc(
                            color = FocusForestGreenDeep,
                            startAngle = -90f,
                            sweepAngle = animatedSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "$goalPercent%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                        )
                        Text(
                            text = stringResource(R.string.home_weekly_goal_label),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF64748B),
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. Inspirational Quote Banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuoteBannerCard(
    quote: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.shadow(
            elevation = 2.dp,
            shape = RoundedCornerShape(20.dp),
            ambientColor = Color(0x06000000),
            spotColor = Color(0x0A000000),
        ),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFEAF4EE),
        border = BorderStroke(1.dp, Color(0xFFD6E8DC)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(20.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left Quote Content
            Column(
                modifier = Modifier
                    .weight(1.35f)
                    .padding(start = 18.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "“",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusForestGreenDeep,
                    lineHeight = 24.sp,
                )
                Text(
                    text = quote,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B),
                    lineHeight = 18.sp,
                )
            }

            // Right Mountain Sunrise Artwork
            Image(
                painter = painterResource(id = R.drawable.bg_quote_mountain),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterEnd,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialogs: Custom Duration & Focus Mode
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CustomDurationDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var sliderValue by remember { mutableFloatStateOf(currentMinutes.toFloat().coerceIn(5f, 180f)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.home_custom_dialog_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${sliderValue.toInt()} min",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusForestGreenDeep,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 5f..180f,
                    steps = 34, // 5 min increments
                    colors = SliderDefaults.colors(
                        thumbColor = FocusForestGreenDeep,
                        activeTrackColor = FocusForestGreen,
                        inactiveTrackColor = Color(0xFFE2E8F0),
                    ),
                )
                Text(
                    text = "5 min – 180 min",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(sliderValue.toInt()) }) {
                Text(
                    text = stringResource(R.string.home_custom_dialog_confirm),
                    fontWeight = FontWeight.Bold,
                    color = FocusForestGreenDeep,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.home_custom_dialog_cancel),
                    color = Color(0xFF64748B),
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
    )
}

@Composable
private fun FocusModeDialog(
    selectedMode: FocusMode,
    onSelectMode: (FocusMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.home_section_focus_mode),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FocusModeOption(
                    title = stringResource(R.string.home_mode_work_title),
                    subtitle = stringResource(R.string.home_mode_work_subtitle),
                    isSelected = selectedMode == FocusMode.WORK,
                    onClick = { onSelectMode(FocusMode.WORK) },
                )
                FocusModeOption(
                    title = stringResource(R.string.home_mode_study_title),
                    subtitle = stringResource(R.string.home_mode_study_subtitle),
                    isSelected = selectedMode == FocusMode.STUDY,
                    onClick = { onSelectMode(FocusMode.STUDY) },
                )
                FocusModeOption(
                    title = stringResource(R.string.home_mode_personal_title),
                    subtitle = stringResource(R.string.home_mode_personal_subtitle),
                    isSelected = selectedMode == FocusMode.PERSONAL,
                    onClick = { onSelectMode(FocusMode.PERSONAL) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.home_custom_dialog_cancel), color = Color(0xFF64748B))
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
    )
}

@Composable
private fun FocusModeOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                indication = ripple(bounded = true),
                interactionSource = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) FocusForestMintBg else Color(0xFFF8FAF8),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) FocusForestMintBorder else Color(0xFFE2E8F0),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) FocusForestGreenDeep else Color(0xFF0F172A),
                )
                Text(
                    text = subtitle.replace("\n", " "),
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                )
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Home — Reference Design")
@Composable
private fun HomeScreenReferencePreview() {
    FocusFlowTheme(darkTheme = false) {
        HomeScreenContent(uiState = HomeUiState())
    }
}
