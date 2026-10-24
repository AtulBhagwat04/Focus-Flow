package com.focusflow.feature.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.R as DesignR
import com.focusflow.core.designsystem.component.CustomDurationDialog
import com.focusflow.core.designsystem.component.FocusFlowActionButton
import com.focusflow.core.designsystem.component.FocusFlowAppBar
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.OutfitFontFamily
import com.focusflow.core.domain.model.AmbientSound
import com.focusflow.feature.timer.components.ActiveTaskFocusCard
import com.focusflow.feature.timer.components.AmbientSoundCard
import com.focusflow.feature.timer.components.AmbientSoundDialog
import com.focusflow.feature.timer.components.BlockAppsCard
import com.focusflow.feature.timer.components.CompletionDialog
import com.focusflow.feature.timer.components.FocusDialDisplay
import com.focusflow.feature.timer.components.ScenicQuoteCard
import com.focusflow.feature.timer.components.TimerPresetChips

private val START_BUTTON_HEIGHT = 52.dp
private val BUTTON_CORNER_RADIUS = 26.dp

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = hiltViewModel(),
    onNavigateToLimits: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TimerScreenContent(
        uiState = uiState,
        callbacks = TimerUiCallbacks(
            onStartClicked = viewModel::onStartClicked,
            onPauseClicked = viewModel::onPauseClicked,
            onResumeClicked = viewModel::onResumeClicked,
            onCompleteClicked = viewModel::onCompleteClicked,
            onSelectMode = viewModel::onSelectMode,
            onSelectTag = viewModel::onSelectTag,
            onSelectSound = viewModel::onSelectSound,
            onDismissCelebration = viewModel::onDismissCelebration,
            onSelectDurationMinutes = viewModel::onSelectDurationMinutes,
            onToggleBlockApps = viewModel::onToggleBlockApps,
            onAbortClicked = viewModel::onAbortClicked,
            onClearActiveTask = viewModel::onClearActiveTask,
        ),
        onNavigateToLimits = onNavigateToLimits,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier,
    )
}

@Composable
fun TimerScreenContent(
    uiState: TimerUiState,
    callbacks: TimerUiCallbacks,
    modifier: Modifier = Modifier,
    onNavigateToLimits: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var showAmbientSoundDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FBF9)),
    ) {
        // ── 1. Full Screen Scenic Nature Background ──────────────────────────
        Image(
            painter = painterResource(id = DesignR.drawable.bg_home_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
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

        // ── 3. Foreground Layout ─────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            FocusFlowAppBar(
                actions = {
                    val isSoundActive = uiState.selectedSound != AmbientSound.NONE
                    FocusFlowActionButton(
                        icon = Icons.Outlined.Spa,
                        onClick = { showAmbientSoundDialog = true },
                        contentDescription = stringResource(R.string.timer_cd_ambient_sound),
                        containerColor = if (isSoundActive) FocusForestGreenDeep else FocusForestMintBg,
                        iconTint = if (isSoundActive) Color.White else FocusForestGreenDeep,
                    )
                    Box {
                        FocusFlowActionButton(
                            icon = Icons.Default.MoreVert,
                            onClick = { showMenu = true },
                            contentDescription = stringResource(R.string.timer_cd_more_options),
                        )

                        // Refined Dropdown Menu from More Options button
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = RoundedCornerShape(18.dp),
                            containerColor = Color.White,
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.2.dp, Color(0xFFCCE3D5)),
                            modifier = Modifier.width(220.dp),
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.timer_ambient_sound),
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF14291E),
                                        )
                                        Text(
                                            text = if (uiState.selectedSound != AmbientSound.NONE) uiState.selectedSound.displayName else "Off",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.5.sp,
                                            color = if (uiState.selectedSound != AmbientSound.NONE) FocusForestGreen else FocusMutedText,
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.GraphicEq,
                                        contentDescription = null,
                                        tint = FocusForestGreenDeep,
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showAmbientSoundDialog = true
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.timer_menu_blocked_apps),
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF14291E),
                                        )
                                        Text(
                                            text = if (uiState.isBlockAppsEnabled) "Enabled" else "Off",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.5.sp,
                                            color = if (uiState.isBlockAppsEnabled) FocusForestGreen else FocusMutedText,
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Block,
                                        contentDescription = null,
                                        tint = FocusForestGreenDeep,
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToLimits()
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.timer_menu_settings),
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF14291E),
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = null,
                                        tint = FocusForestGreenDeep,
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                },
                            )
                            if (uiState.isActive) {
                                HorizontalDivider(
                                    color = Color(0xFFE2EBE5),
                                    thickness = 0.8.dp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.timer_menu_reset),
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFDC2626),
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Refresh,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(20.dp),
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        callbacks.onAbortClicked()
                                    },
                                )
                            }
                        }
                    }
                },
                containerColor = Color.Transparent,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // ── Title & Quote ─────────────────────────────────────────────
                Text(
                    text = stringResource(R.string.timer_screen_title),
                    fontSize = 30.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14291E),
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.semantics { heading() },
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Motivational quote positioned directly below Focus Time text
                ScenicQuoteCard(
                    quote = uiState.quote,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ── Main Circular Focus Dial (complete circle = 1 hr, swiping enabled)
                FocusDialDisplay(
                    formattedTime = uiState.formattedDisplayTime,
                    statusLabel = when {
                        uiState.isPaused -> stringResource(R.string.timer_session_paused)
                        uiState.isRunning -> if (uiState.activeTask != null) uiState.activeTask.title else stringResource(R.string.timer_session_running)
                        uiState.activeTask != null -> uiState.activeTask.title
                        else -> stringResource(R.string.timer_session_label)
                    },
                    isActive = uiState.isActive,
                    progressFraction = uiState.progress,
                    selectedDurationMinutes = uiState.selectedDurationMinutes,
                    isCustomTimeEnabled = !uiState.isActive && uiState.activeTask == null,
                    onDurationChange = callbacks.onSelectDurationMinutes,
                    onCustomTimeClick = {
                        if (!uiState.isActive && uiState.activeTask == null) {
                            showCustomDurationDialog = true
                        }
                    },
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ── Predefined Timer Chips (25, 50, 100) below Circular Dial (hidden when focusing by task)
                if (uiState.activeTask == null) {
                    TimerPresetChips(
                        selectedMinutes = uiState.selectedDurationMinutes,
                        onSelectMinutes = callbacks.onSelectDurationMinutes,
                        enabled = !uiState.isActive,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Active Task Focus Card directly above Start Focus button ──
                if (uiState.activeTask != null) {
                    ActiveTaskFocusCard(
                        task = uiState.activeTask,
                        isRunning = uiState.isRunning,
                        isPaused = uiState.isPaused,
                        onClearTask = callbacks.onClearActiveTask,
                        canClear = !uiState.isActive,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // ── Primary Action Button ("Start Focus" / Pause / Resume / Finish)
                TimerActionButtons(
                    uiState = uiState,
                    callbacks = callbacks,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Refined Block Apps Card ──────────────────────────────────────
                BlockAppsCard(
                    isEnabled = uiState.isBlockAppsEnabled,
                    onToggle = callbacks.onToggleBlockApps,
                    onNavigateToLimits = onNavigateToLimits,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ── Refined Ambient Sound Card ───────────────────────────────────
                AmbientSoundCard(
                    selectedSound = uiState.selectedSound,
                    onCardClick = { showAmbientSoundDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                )

                // ── Bottom Spacing for navigation ───────────────────────────────
                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(if (uiState.activeTask != null) 32.dp else 96.dp)
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
                callbacks.onSelectDurationMinutes(minutes)
                showCustomDurationDialog = false
            },
        )
    }

    if (showAmbientSoundDialog) {
        AmbientSoundDialog(
            selectedSound = uiState.selectedSound,
            onSelectSound = callbacks.onSelectSound,
            onDismiss = { showAmbientSoundDialog = false },
        )
    }

    if (uiState.showCelebrationDialog) {
        CompletionDialog(
            xpEarned = uiState.completedXpEarned,
            onDismiss = callbacks.onDismissCelebration,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Subcomponents
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TimerActionButtons(
    uiState: TimerUiState,
    callbacks: TimerUiCallbacks,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            !uiState.isActive -> {
                // Wide Primary Pill: "▶ Start Focus"
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(START_BUTTON_HEIGHT)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                            ambientColor = Color(0x301B4E36),
                            spotColor = Color(0x401B4E36),
                        )
                        .clip(RoundedCornerShape(BUTTON_CORNER_RADIUS))
                        .clickable(
                            indication = ripple(bounded = true),
                            interactionSource = null,
                            onClickLabel = stringResource(R.string.timer_start_focus),
                            onClick = callbacks.onStartClicked,
                        )
                        .semantics {
                            role = Role.Button
                            contentDescription = "Start Focus"
                        },
                    shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
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
                            text = stringResource(R.string.timer_start_focus),
                            fontFamily = OutfitFontFamily,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }

            uiState.isRunning -> {
                // Pause (Solid White Pill) and Finish (Solid Green Pill)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(START_BUTTON_HEIGHT)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                            ambientColor = Color(0x18000000),
                            spotColor = Color(0x20000000),
                        )
                        .clip(RoundedCornerShape(BUTTON_CORNER_RADIUS))
                        .clickable(
                            indication = ripple(bounded = true),
                            interactionSource = null,
                            onClick = callbacks.onPauseClicked,
                        )
                        .semantics { role = Role.Button },
                    shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFFCCE3D5)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Pause,
                            contentDescription = null,
                            tint = FocusForestGreenDeep,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.timer_pause),
                            fontFamily = OutfitFontFamily,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusForestGreenDeep,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(START_BUTTON_HEIGHT)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                            ambientColor = Color(0x301B4E36),
                            spotColor = Color(0x401B4E36),
                        )
                        .clip(RoundedCornerShape(BUTTON_CORNER_RADIUS))
                        .clickable(
                            indication = ripple(bounded = true),
                            interactionSource = null,
                            onClick = callbacks.onCompleteClicked,
                        )
                        .semantics { role = Role.Button },
                    shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                    color = FocusForestGreenDeep,
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.timer_finish),
                            fontFamily = OutfitFontFamily,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }

            uiState.isPaused -> {
                // Resume (Solid Green Pill) and Finish (Solid White Pill)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(START_BUTTON_HEIGHT)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                            ambientColor = Color(0x301B4E36),
                            spotColor = Color(0x401B4E36),
                        )
                        .clip(RoundedCornerShape(BUTTON_CORNER_RADIUS))
                        .clickable(
                            indication = ripple(bounded = true),
                            interactionSource = null,
                            onClick = callbacks.onResumeClicked,
                        )
                        .semantics { role = Role.Button },
                    shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
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
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.timer_resume),
                            fontFamily = OutfitFontFamily,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(START_BUTTON_HEIGHT)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                            ambientColor = Color(0x18000000),
                            spotColor = Color(0x20000000),
                        )
                        .clip(RoundedCornerShape(BUTTON_CORNER_RADIUS))
                        .clickable(
                            indication = ripple(bounded = true),
                            interactionSource = null,
                            onClick = callbacks.onCompleteClicked,
                        )
                        .semantics { role = Role.Button },
                    shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFFCCE3D5)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = FocusForestGreenDeep,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.timer_finish),
                            fontFamily = OutfitFontFamily,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusForestGreenDeep,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Timer — FocusFlow Nature Reference")
@Composable
private fun TimerScreenIdlePreview() {
    FocusFlowTheme {
        TimerScreenContent(
            uiState = TimerUiState(),
            callbacks = TimerUiCallbacks(
                onStartClicked = {},
                onPauseClicked = {},
                onResumeClicked = {},
                onCompleteClicked = {},
                onSelectMode = {},
                onSelectTag = {},
                onSelectSound = {},
                onDismissCelebration = {},
                onSelectDurationMinutes = {},
                onToggleBlockApps = {},
                onAbortClicked = {},
            ),
        )
    }
}
