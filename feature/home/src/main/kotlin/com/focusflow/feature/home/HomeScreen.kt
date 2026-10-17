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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.StrokeJoin
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Locale
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
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
        onStartFocus = {
            viewModel.startFocus()
        },
        onStartFocusWithDuration = { minutes ->
            viewModel.startFocus(minutes)
        },
        onPauseFocus = viewModel::pauseFocus,
        onResumeFocus = viewModel::resumeFocus,
        onStopFocus = viewModel::stopFocus,
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
    onStartFocus: () -> Unit = {},
    onStartFocusWithDuration: (Int) -> Unit = {},
    onPauseFocus: () -> Unit = {},
    onResumeFocus: () -> Unit = {},
    onStopFocus: () -> Unit = {},
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
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ── 1. Greeting + Profile Button Row ──────────────────────────
                HomeGreetingSection(
                    greeting = uiState.greeting,
                    onNavigateToProfile = onNavigateToProfile,
                    profileContentDescription = stringResource(R.string.home_cd_profile),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                )

                // ── 2. Hero Card: "Start a Focus Session" / Active Timer Status ──
                HeroFocusCard(
                    isSessionActive = uiState.isSessionActive,
                    isSessionRunning = uiState.isSessionRunning,
                    isSessionPaused = uiState.isSessionPaused,
                    formattedSessionTime = uiState.formattedSessionTime,
                    sessionRemainingMs = uiState.sessionRemainingMs,
                    sessionProgress = uiState.sessionProgress,
                    selectedDurationMinutes = uiState.selectedDurationMinutes,
                    onSelectDuration = onSelectDuration,
                    onCustomDurationClicked = { showCustomDurationDialog = true },
                    onStartFocus = onStartFocus,
                    onPauseFocus = onPauseFocus,
                    onResumeFocus = onResumeFocus,
                    onStopFocus = onStopFocus,
                    onOpenFocusScreen = onNavigateToFocus,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ── 3. Quick Actions Row ─────────────────────────────────────────
                QuickActionsSection(
                    onNavigateToLimits = onNavigateToLimits,
                    onNavigateToStats = onNavigateToStats,
                    onNavigateToTasks = onNavigateToTasks,
                    onFocusModeClicked = { showFocusModeDialog = true },
                    onSeeAllClicked = onNavigateToLimits,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ── 4. Weekly Progress Card ──────────────────────────────────────
                WeeklyProgressCard(
                    focusedFormatted = uiState.weeklyFocusFormatted,
                    goalPercent = uiState.weeklyGoalPercent,
                    dailyHours = uiState.weeklyDailyHours,
                    onViewDetails = onNavigateToStats,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ── 5. Inspirational Quote Banner ────────────────────────────────
                QuoteBannerCard(
                    quote = uiState.quote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                )

                // ── 6. Bottom Navigation Bar Spacing ─────────────────────────────
                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(60.dp)
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
                onStartFocusWithDuration(minutes)
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
    isSessionActive: Boolean,
    isSessionRunning: Boolean,
    isSessionPaused: Boolean,
    formattedSessionTime: String,
    sessionRemainingMs: Long,
    sessionProgress: Float,
    selectedDurationMinutes: Int,
    onSelectDuration: (Int) -> Unit,
    onCustomDurationClicked: () -> Unit,
    onStartFocus: () -> Unit,
    onPauseFocus: () -> Unit,
    onResumeFocus: () -> Unit,
    onStopFocus: () -> Unit,
    onOpenFocusScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startFocusActionLabel = stringResource(R.string.home_hero_start_btn)
    val minUnitLabel = stringResource(R.string.home_hero_min_label)

    Surface(
        modifier = modifier.shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(26.dp),
            ambientColor = Color(0x0C000000),
            spotColor = Color(0x18000000),
        ),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        border = BorderStroke(1.dp, FocusCardBorder),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp)),
        ) {
            // Calm background photo on the card with warm daylight and plants
            Image(
                painter = painterResource(id = R.drawable.bg_hero_focus_card),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.matchParentSize(),
            )

            // Warm subtle scrim preserving the natural light and leaf silhouettes
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.White.copy(alpha = 0.88f),
                            0.45f to Color(0xFFFAF9F6).copy(alpha = 0.84f),
                            1.0f to Color(0xFFFAF9F6).copy(alpha = 0.88f),
                        ),
                    ),
            )

            if (isSessionActive) {
                // ── Active Focus Session Status ─────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    // Row 1: Status Pill ("● Focusing" / "❚❚ Paused") + "Focus Screen >" link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSessionRunning) Color(0xFFD4F3E1) else Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, if (isSessionRunning) Color(0xFFBAE6CD) else Color(0xFFFDE68A)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isSessionRunning) Color(0xFF16A34A) else Color(0xFFD97706)),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSessionRunning) stringResource(R.string.home_session_active_focusing)
                                           else stringResource(R.string.home_session_active_paused),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSessionRunning) Color(0xFF14532D) else Color(0xFF92400E),
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(
                                    indication = ripple(bounded = true),
                                    interactionSource = null,
                                    onClick = onOpenFocusScreen,
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.home_session_open_focus_screen),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FocusForestGreenDeep,
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = FocusForestGreenDeep,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Live Countdown + Subtitle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column {
                            Text(
                                text = formattedSessionTime,
                                fontFamily = FontFamily.Serif,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF14291E),
                                letterSpacing = (-0.5).sp,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val remainingMinutes = ((sessionRemainingMs + 59_999L) / 60_000L).coerceAtLeast(0L)
                            Text(
                                text = "$selectedDurationMinutes min • $remainingMinutes min ${stringResource(R.string.home_session_remaining)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF4A5D52),
                            )
                        }

                        Text(
                            text = "${(sessionProgress.coerceIn(0f, 1f) * 100).toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusForestGreenDeep,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 3: Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(Color(0xFFE4ECE6)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = sessionProgress.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(2.5.dp))
                                .background(FocusForestGreenDeep),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 4: Controls (Pause/Resume + End)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Pause or Resume
                        Surface(
                            modifier = Modifier
                                .weight(1.4f)
                                .height(42.dp)
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(21.dp),
                                    ambientColor = Color(0x201B4E36),
                                    spotColor = Color(0x351B4E36),
                                )
                                .clip(RoundedCornerShape(21.dp))
                                .clickable(
                                    indication = ripple(bounded = true),
                                    interactionSource = null,
                                    onClick = if (isSessionRunning) onPauseFocus else onResumeFocus,
                                )
                                .semantics { role = Role.Button },
                            shape = RoundedCornerShape(21.dp),
                            color = FocusForestGreenDeep,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = if (isSessionRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSessionRunning) stringResource(R.string.home_session_btn_pause)
                                           else stringResource(R.string.home_session_btn_resume),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }

                        // End Session
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .clickable(
                                    indication = ripple(bounded = true),
                                    interactionSource = null,
                                    onClick = onStopFocus,
                                )
                                .semantics { role = Role.Button },
                            shape = RoundedCornerShape(21.dp),
                            color = Color.White,
                            border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.home_session_btn_end),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569),
                                )
                            }
                        }
                    }
                }
            } else {
                // ── Idle Session Start Config ────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = stringResource(R.string.home_hero_title),
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14291E),
                        letterSpacing = (-0.3).sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(R.string.home_hero_subtitle),
                        fontFamily = FontFamily.Serif,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF4A5D52),
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Duration Selector Cards: 25, 50, 90, Custom
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // "Start Focus" CTA Button (Wide Pill)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Color(0x201B4E36),
                                spotColor = Color(0x351B4E36),
                            )
                            .clip(RoundedCornerShape(22.dp))
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
                        shape = RoundedCornerShape(22.dp),
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
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = startFocusActionLabel,
                                fontFamily = FontFamily.Serif,
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
    val numberColor = if (isSelected) Color.White else Color(0xFF14291E)
    val unitColor = if (isSelected) Color(0xD9FFFFFF) else Color(0xFF64748B)
    val borderColor = if (isSelected) FocusForestGreenDeep else Color(0xFFE4ECE6)

    Surface(
        modifier = modifier
            .height(46.dp)
            .shadow(
                elevation = if (isSelected) 3.dp else 1.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = if (isSelected) Color(0x201B4E36) else Color(0x06000000),
                spotColor = if (isSelected) Color(0x351B4E36) else Color(0x0C000000),
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                indication = ripple(bounded = true),
                interactionSource = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.RadioButton
                this.contentDescription = "$durationNumber $unitLabel"
            },
        shape = RoundedCornerShape(12.dp),
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
                fontFamily = FontFamily.Serif,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = numberColor,
                lineHeight = 16.sp,
            )
            Text(
                text = unitLabel,
                fontFamily = FontFamily.Serif,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = unitColor,
                lineHeight = 11.sp,
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
    val borderColor = if (isSelected) FocusForestGreenDeep else Color(0xFFE4ECE6)
    val customCd = if (isSelected) "$customMinutes $unitLabel" else stringResource(R.string.home_hero_custom)

    Surface(
        modifier = modifier
            .height(46.dp)
            .shadow(
                elevation = if (isSelected) 3.dp else 1.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = if (isSelected) Color(0x201B4E36) else Color(0x06000000),
                spotColor = if (isSelected) Color(0x351B4E36) else Color(0x0C000000),
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                indication = ripple(bounded = true),
                interactionSource = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.RadioButton
                this.contentDescription = customCd
            },
        shape = RoundedCornerShape(12.dp),
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
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 16.sp,
                )
                Text(
                    text = unitLabel,
                    fontFamily = FontFamily.Serif,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xD9FFFFFF),
                    lineHeight = 11.sp,
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.home_hero_custom),
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14291E),
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
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                letterSpacing = (-0.3).sp,
            )

            // "See All >" Pill Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        indication = ripple(bounded = true),
                        interactionSource = null,
                        onClick = onSeeAllClicked,
                    )
                    .semantics { role = Role.Button },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEFF5F1),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.home_action_see_all),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Row of Compact Action Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuickActionPill(
                title = stringResource(R.string.home_action_block_apps),
                containerColor = Color(0xFFFDF1F0),
                borderColor = Color(0xFFFCDADA),
                iconBadgeBg = Color(0xFFFDDCDC),
                iconContent = {
                    Icon(
                        imageVector = Icons.Outlined.Block,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(15.dp),
                    )
                },
                onClick = onNavigateToLimits,
            )

            QuickActionPill(
                title = stringResource(R.string.home_action_focus_stats),
                containerColor = Color(0xFFEDF7F1),
                borderColor = Color(0xFFD6EDE0),
                iconBadgeBg = Color(0xFFD4F3E1),
                iconContent = {
                    BarChartStatsIcon(
                        tint = Color(0xFF144D31),
                        modifier = Modifier.size(15.dp),
                    )
                },
                onClick = onNavigateToStats,
            )

            QuickActionPill(
                title = stringResource(R.string.home_action_tasks),
                containerColor = Color(0xFFEFF5FE),
                borderColor = Color(0xFFDCEAFE),
                iconBadgeBg = Color(0xFFDCEAFE),
                iconContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Assignment,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(15.dp),
                    )
                },
                onClick = onNavigateToTasks,
            )

            QuickActionPill(
                title = stringResource(R.string.home_action_focus_mode_card),
                containerColor = Color(0xFFEDF7F1),
                borderColor = Color(0xFFD6EDE0),
                iconBadgeBg = Color(0xFFD4F3E1),
                iconContent = {
                    SproutIcon(
                        tint = Color(0xFF144D31),
                        modifier = Modifier.size(15.dp),
                    )
                },
                onClick = onFocusModeClicked,
            )
        }
    }
}

@Composable
private fun QuickActionPill(
    title: String,
    containerColor: Color,
    borderColor: Color,
    iconBadgeBg: Color,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x06000000),
                spotColor = Color(0x0A000000),
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                indication = ripple(bounded = true),
                interactionSource = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Button
                this.contentDescription = title
            },
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .padding(start = 6.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon Badge Circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconBadgeBg),
                contentAlignment = Alignment.Center,
            ) {
                iconContent()
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Title
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Right Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

@Composable
private fun BarChartStatsIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barWidth = (w * 0.20f).coerceAtLeast(1.5f)
        val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

        // Left bar (medium-short)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.12f, h * 0.44f),
            size = Size(barWidth, h * 0.46f),
            cornerRadius = cornerRadius,
        )

        // Center bar (tallest)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.40f, h * 0.16f),
            size = Size(barWidth, h * 0.74f),
            cornerRadius = cornerRadius,
        )

        // Right bar (medium)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.68f, h * 0.28f),
            size = Size(barWidth, h * 0.62f),
            cornerRadius = cornerRadius,
        )
    }
}

@Composable
private fun SproutIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeW = (size.width * 0.10f).coerceAtLeast(1.5f)
        val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = size.width
        val h = size.height

        // Stem
        drawLine(
            color = tint,
            start = Offset(w * 0.5f, h * 0.88f),
            end = Offset(w * 0.5f, h * 0.46f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round,
        )

        // Left leaf outline
        val leftLeaf = Path().apply {
            moveTo(w * 0.5f, h * 0.54f)
            cubicTo(
                w * 0.28f, h * 0.65f,
                w * 0.06f, h * 0.35f,
                w * 0.12f, h * 0.18f,
            )
            cubicTo(
                w * 0.28f, h * 0.12f,
                w * 0.44f, h * 0.32f,
                w * 0.5f, h * 0.54f,
            )
        }
        drawPath(leftLeaf, tint, style = stroke)

        // Right leaf outline
        val rightLeaf = Path().apply {
            moveTo(w * 0.5f, h * 0.46f)
            cubicTo(
                w * 0.72f, h * 0.56f,
                w * 0.94f, h * 0.30f,
                w * 0.88f, h * 0.15f,
            )
            cubicTo(
                w * 0.72f, h * 0.08f,
                w * 0.56f, h * 0.28f,
                w * 0.5f, h * 0.46f,
            )
        }
        drawPath(rightLeaf, tint, style = stroke)
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
            shape = RoundedCornerShape(18.dp),
            ambientColor = Color(0x0A000000),
            spotColor = Color(0x10000000),
        ),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, FocusCardBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
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
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.home_weekly_title),
                        fontSize = 15.5.sp,
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp,
                    )
                    Text(
                        text = stringResource(R.string.home_weekly_focused_this_week),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF64748B),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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
                            val targetBarDp = (42 * barHeightFraction).dp

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(13.dp)
                                        .height(targetBarDp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 5.dp,
                                                topEnd = 5.dp,
                                                bottomStart = 3.dp,
                                                bottomEnd = 3.dp
                                            )
                                        )
                                        .background(Color(0xFF76A88E)),
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = day,
                                    fontSize = 10.sp,
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
                        .size(72.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val animatedSweep by animateFloatAsState(
                        targetValue = (goalPercent / 100f) * 360f,
                        animationSpec = tween(durationMillis = 800),
                        label = "progress_sweep",
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 6.5.dp.toPx()
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                        )
                        Text(
                            text = stringResource(R.string.home_weekly_goal_label),
                            fontSize = 10.5.sp,
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
            shape = RoundedCornerShape(18.dp),
            ambientColor = Color(0x06000000),
            spotColor = Color(0x0A000000),
        ),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFEAF4EE),
        border = BorderStroke(1.dp, Color(0xFFD6E8DC)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .clip(RoundedCornerShape(18.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left Quote Content
            Column(
                modifier = Modifier
                    .weight(1.4f)
                    .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "“",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusForestGreenDeep,
                    lineHeight = 20.sp,
                )
                Text(
                    text = quote,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B),
                    lineHeight = 16.sp,
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
    val initialHours = (currentMinutes / 60).coerceIn(0, 12)
    val initialMinutes = (currentMinutes % 60).coerceIn(0, 59)
    var hoursText by remember { mutableStateOf(if (currentMinutes <= 0) "00" else "%02d".format(initialHours)) }
    var minutesText by remember { mutableStateOf(if (currentMinutes <= 0) "30" else "%02d".format(initialMinutes)) }

    val hoursLabel = stringResource(R.string.home_custom_dialog_hours)
    val minutesLabel = stringResource(R.string.home_custom_dialog_minutes)
    val incHoursCd = stringResource(R.string.home_custom_dialog_inc_hours)
    val decHoursCd = stringResource(R.string.home_custom_dialog_dec_hours)
    val incMinutesCd = stringResource(R.string.home_custom_dialog_inc_minutes)
    val decMinutesCd = stringResource(R.string.home_custom_dialog_dec_minutes)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color(0x15000000),
                    spotColor = Color(0x25000000),
                ),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 1. Botanical Header with Clock (Cross symbol removed per request)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BotanicalLeafSprig(
                        modifier = Modifier.size(width = 46.dp, height = 36.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BotanicalClockBadge()
                    Spacer(modifier = Modifier.width(6.dp))
                    BotanicalLeafSprig(
                        modifier = Modifier
                            .size(width = 46.dp, height = 36.dp)
                            .graphicsLayer { scaleX = -1f },
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Title & Subtitle
                Text(
                    text = stringResource(R.string.home_custom_dialog_title),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF14291E),
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.3).sp,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.home_custom_dialog_subtitle),
                    fontFamily = FontFamily.Serif,
                    fontSize = 13.5.sp,
                    color = Color(0xFF5A6E65),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 3. Time Steppers Container with manual entry
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF3F7F4),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            // Hours Card with manual typing
                            TimeStepperBox(
                                valueText = hoursText,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }
                                    hoursText = if (digits.length > 2) digits.takeLast(2) else digits
                                },
                                onIncrement = {
                                    val current = hoursText.toIntOrNull() ?: 0
                                    val newH = (current + 1).coerceAtMost(12)
                                    hoursText = "%02d".format(newH)
                                },
                                onDecrement = {
                                    val current = hoursText.toIntOrNull() ?: 0
                                    val newH = (current - 1).coerceAtLeast(0)
                                    hoursText = "%02d".format(newH)
                                },
                                incCd = incHoursCd,
                                decCd = decHoursCd,
                                modifier = Modifier.weight(1f),
                            )

                            // Colon separator
                            Text(
                                text = ":",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = Color(0xFF14291E),
                                modifier = Modifier.padding(horizontal = 10.dp),
                            )

                            // Minutes Card with manual typing
                            TimeStepperBox(
                                valueText = minutesText,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }
                                    minutesText = if (digits.length > 2) digits.takeLast(2) else digits
                                },
                                onIncrement = {
                                    val currentM = minutesText.toIntOrNull() ?: 0
                                    val currentH = hoursText.toIntOrNull() ?: 0
                                    val next = currentM + 5
                                    if (next >= 60) {
                                        minutesText = "00"
                                        if (currentH < 12) {
                                            hoursText = "%02d".format(currentH + 1)
                                        }
                                    } else {
                                        minutesText = "%02d".format(next)
                                    }
                                },
                                onDecrement = {
                                    val currentM = minutesText.toIntOrNull() ?: 0
                                    val currentH = hoursText.toIntOrNull() ?: 0
                                    if (currentM >= 5) {
                                        minutesText = "%02d".format(currentM - 5)
                                    } else if (currentH > 0) {
                                        hoursText = "%02d".format(currentH - 1)
                                        minutesText = "55"
                                    } else {
                                        minutesText = "05"
                                    }
                                },
                                incCd = incMinutesCd,
                                decCd = decMinutesCd,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Labels: "Hours" & "Minutes"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = hoursLabel,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = Color(0xFF5A6E65),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                            )

                            Spacer(modifier = Modifier.width(28.dp))

                            Text(
                                text = minutesLabel,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = Color(0xFF5A6E65),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Action Buttons (Cancel + Start Focus)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Cancel Button
                    Surface(
                        modifier = Modifier
                            .weight(0.40f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(
                                indication = ripple(bounded = true),
                                interactionSource = null,
                                onClick = onDismiss,
                            )
                            .semantics { role = Role.Button },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.home_custom_dialog_cancel),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color(0xFF1E293B),
                            )
                        }
                    }

                    // Start Focus Button
                    Surface(
                        modifier = Modifier
                            .weight(0.60f)
                            .height(48.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = Color(0x201B4E36),
                                spotColor = Color(0x351B4E36),
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(
                                indication = ripple(bounded = true),
                                interactionSource = null,
                                onClick = {
                                    val h = (hoursText.toIntOrNull() ?: 0).coerceIn(0, 12)
                                    val m = (minutesText.toIntOrNull() ?: 0).coerceIn(0, 59)
                                    val totalMinutes = (h * 60 + m).coerceAtLeast(5)
                                    onConfirm(totalMinutes)
                                },
                            )
                            .semantics { role = Role.Button },
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
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.home_custom_dialog_start),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeStepperBox(
    valueText: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    incCd: String,
    decCd: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0x08000000),
                spotColor = Color(0x0F000000),
            ),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2EBE5)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (valueText.isEmpty()) {
                    Text(
                        text = "00",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color(0xFFCBD5E1),
                    )
                }
                BasicTextField(
                    value = valueText,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color(0xFF14291E),
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    cursorBrush = SolidColor(FocusForestGreenDeep),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropUp,
                    contentDescription = incCd,
                    tint = Color(0xFF14291E),
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(
                            indication = ripple(bounded = true, radius = 12.dp),
                            interactionSource = null,
                            onClick = onIncrement,
                        ),
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = decCd,
                    tint = Color(0xFF14291E),
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(
                            indication = ripple(bounded = true, radius = 12.dp),
                            interactionSource = null,
                            onClick = onDecrement,
                        ),
                )
            }
        }
    }
}

@Composable
private fun BotanicalClockBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color(0xFFEAF3ED)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            val r = size.minDimension / 2f
            val cx = size.width / 2f
            val cy = size.height / 2f
            val strokeW = 2.4.dp.toPx()
            val clockColor = Color(0xFF14291E)

            // Outer dial ring
            drawCircle(
                color = clockColor,
                radius = r - strokeW / 2,
                style = Stroke(width = strokeW),
            )

            // Center hub
            drawCircle(
                color = clockColor,
                radius = 2.2.dp.toPx(),
            )

            // Hour hand pointing straight up (12:00)
            drawLine(
                color = clockColor,
                start = Offset(cx, cy),
                end = Offset(cx, cy - r * 0.52f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round,
            )

            // Minute hand pointing to ~5:24
            val minuteAngleRad = Math.toRadians(70.0).toFloat()
            val minuteLen = r * 0.62f
            drawLine(
                color = clockColor,
                start = Offset(cx, cy),
                end = Offset(
                    cx + cos(minuteAngleRad) * minuteLen,
                    cy + sin(minuteAngleRad) * minuteLen,
                ),
                strokeWidth = strokeW,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun BotanicalLeafSprig(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Stem: gentle curve from right to left
        val stemPath = Path().apply {
            moveTo(w * 0.95f, h * 0.72f)
            quadraticTo(w * 0.55f, h * 0.62f, w * 0.15f, h * 0.45f)
        }
        drawPath(
            path = stemPath,
            color = Color(0xFF729A82),
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round),
        )

        fun drawLeaf(
            baseX: Float, baseY: Float,
            tipX: Float, tipY: Float,
            width: Float,
            fillColor: Color,
        ) {
            val dx = tipX - baseX
            val dy = tipY - baseY
            val len = hypot(dx, dy).coerceAtLeast(1f)
            val nx = (-dy / len) * width
            val ny = (dx / len) * width

            val midX = baseX + dx * 0.45f
            val midY = baseY + dy * 0.45f

            val leafPath = Path().apply {
                moveTo(baseX, baseY)
                quadraticTo(midX + nx, midY + ny, tipX, tipY)
                quadraticTo(midX - nx, midY - ny, baseX, baseY)
                close()
            }
            drawPath(path = leafPath, color = fillColor)

            // Subtle center vein
            drawLine(
                color = fillColor.copy(alpha = 0.55f),
                start = Offset(baseX, baseY),
                end = Offset(baseX + dx * 0.8f, baseY + dy * 0.8f),
                strokeWidth = 0.8.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        // Leaf 1: Upper-right (near badge)
        drawLeaf(
            baseX = w * 0.82f, baseY = h * 0.65f,
            tipX = w * 0.72f, tipY = h * 0.18f,
            width = 6.dp.toPx(),
            fillColor = Color(0xFF8EAF99),
        )

        // Leaf 2: Lower-middle
        drawLeaf(
            baseX = w * 0.62f, baseY = h * 0.62f,
            tipX = w * 0.48f, tipY = h * 0.92f,
            width = 5.dp.toPx(),
            fillColor = Color(0xFF6B937A),
        )

        // Leaf 3: Upper-left
        drawLeaf(
            baseX = w * 0.46f, baseY = h * 0.54f,
            tipX = w * 0.28f, tipY = h * 0.12f,
            width = 6.5.dp.toPx(),
            fillColor = Color(0xFF98BDA3),
        )

        // Leaf 4: Terminal tip
        drawLeaf(
            baseX = w * 0.22f, baseY = h * 0.47f,
            tipX = w * 0.02f, tipY = h * 0.38f,
            width = 5.dp.toPx(),
            fillColor = Color(0xFF7BA48C),
        )
    }
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

@Preview(showBackground = true, name = "Custom Duration Dialog")
@Composable
private fun CustomDurationDialogPreview() {
    FocusFlowTheme(darkTheme = false) {
        CustomDurationDialog(
            currentMinutes = 30,
            onDismiss = {},
            onConfirm = {},
        )
    }
}
