package com.focusflow.feature.home.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusForestMintBorder
import com.focusflow.core.designsystem.theme.FocusInactiveIcon
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.feature.home.R

/**
 * Stateful entry point for the Onboarding screen.
 */
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onCompleteOnboarding: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onCompleteOnboarding()
        }
    }

    BackHandler(enabled = uiState.currentStep > 1) {
        viewModel.previousStep()
    }

    OnboardingScreenContent(
        uiState = uiState,
        onNextStep = viewModel::nextStep,
        onSkip = viewModel::skip,
        onSelectGoal = viewModel::selectGoal,
        onToggleDistraction = viewModel::toggleDistraction,
        onSelectDailyTarget = viewModel::selectDailyTarget,
        modifier = modifier,
    )
}

/**
 * Stateless composable displaying the 5 onboarding steps.
 */
@Composable
fun OnboardingScreenContent(
    uiState: OnboardingUiState,
    onNextStep: () -> Unit,
    onSkip: () -> Unit,
    onSelectGoal: (OnboardingGoal) -> Unit,
    onToggleDistraction: (DistractionType) -> Unit,
    onSelectDailyTarget: (DailyFocusTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            // ── Top Header: 5-Segment Progress Bar + Skip Button ─────────────
            OnboardingTopBar(
                currentStep = uiState.currentStep,
                totalSteps = uiState.totalSteps,
                onSkip = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            )

            // ── Animated Step Content ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                AnimatedContent(
                    targetState = uiState.currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn(tween(300)))
                                .togetherWith(slideOutHorizontally { width -> -width } + fadeOut(tween(300)))
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn(tween(300)))
                                .togetherWith(slideOutHorizontally { width -> width } + fadeOut(tween(300)))
                        }
                    },
                    label = "onboarding_step_transition",
                ) { step ->
                    when (step) {
                        1 -> OnboardingStep1Welcome(
                            onGetStarted = onNextStep,
                            modifier = Modifier.fillMaxSize(),
                        )
                        2 -> OnboardingStep2Goals(
                            selectedGoal = uiState.selectedGoal,
                            onSelectGoal = onSelectGoal,
                            onContinue = onNextStep,
                            modifier = Modifier.fillMaxSize(),
                        )
                        3 -> OnboardingStep3Distractions(
                            selectedDistractions = uiState.selectedDistractions,
                            onToggleDistraction = onToggleDistraction,
                            onContinue = onNextStep,
                            modifier = Modifier.fillMaxSize(),
                        )
                        4 -> OnboardingStep4DailyTarget(
                            selectedDailyTarget = uiState.selectedDailyTarget,
                            onSelectDailyTarget = onSelectDailyTarget,
                            onContinue = onNextStep,
                            modifier = Modifier.fillMaxSize(),
                        )
                        5 -> OnboardingStep5PlanReady(
                            selectedGoal = uiState.selectedGoal,
                            selectedDailyTarget = uiState.selectedDailyTarget,
                            onStartPlan = onNextStep,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top header with 5-segment progress bar and "Skip" button.
 */
@Composable
private fun OnboardingTopBar(
    currentStep: Int,
    totalSteps: Int,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Segmented Progress Bar
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (index in 1..totalSteps) {
                val isFilled = index <= currentStep
                val animatedColor by animateColorAsState(
                    targetValue = if (isFilled) FocusForestGreen else Color(0xFFE2E7E4),
                    label = "segment_color_$index",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(animatedColor),
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Skip Button (visible for steps 1 to 4)
        if (currentStep < totalSteps) {
            TextButton(
                onClick = onSkip,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6F8179),
                )
            }
        } else {
            // Invisible placeholder for alignment consistency
            Spacer(modifier = Modifier.width(44.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 1: Welcome & Value Proposition
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingStep1Welcome(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.home_brand_focus),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = FocusDarkText,
            )
            Text(
                text = stringResource(R.string.home_brand_flow),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = FocusForestGreen,
            )
        }
        Text(
            text = stringResource(R.string.home_tagline),
            fontSize = 13.sp,
            color = FocusMutedText,
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Headline
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Let's build your",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FocusDarkText,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "focus",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusForestGreen,
                )
                Text(
                    text = " routine.",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusDarkText,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.onboarding_s1_subtitle),
                fontSize = 14.sp,
                color = FocusMutedText,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Scenic Nature Artwork
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_home_screen),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // Soft gradient overlay to blend into UI
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x33FFFFFF),
                                Color(0x66FFFFFF),
                            ),
                        ),
                    ),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Button
        OnboardingPrimaryButton(
            text = stringResource(R.string.onboarding_get_started),
            onClick = onGetStarted,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 2: Main Goal Selection ("What do you want to improve?")
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingStep2Goals(
    selectedGoal: OnboardingGoal,
    onSelectGoal: (OnboardingGoal) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Circular Badge Icon
        StepIconBadge(icon = Icons.Outlined.TrackChanges)

        Spacer(modifier = Modifier.height(18.dp))

        // Headline
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "What do you want",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FocusDarkText,
                modifier = Modifier.semantics { heading() },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "to ",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusDarkText,
                )
                Text(
                    text = "improve",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusForestGreen,
                )
                Text(
                    text = "?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusDarkText,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.onboarding_s2_subtitle),
                fontSize = 14.sp,
                color = FocusMutedText,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4 Selectable Goal Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OnboardingGoal.entries.forEach { goal ->
                GoalSelectableCard(
                    goal = goal,
                    isSelected = selectedGoal == goal,
                    onSelect = { onSelectGoal(goal) },
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(28.dp))

        OnboardingPrimaryButton(
            text = stringResource(R.string.onboarding_continue),
            onClick = onContinue,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GoalSelectableCard(
    goal: OnboardingGoal,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) FocusForestMintBg else Color.White,
        label = "goal_bg",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) FocusForestMintBorder else Color(0xFFE5ECE8),
        label = "goal_border",
    )
    val badgeBg = if (isSelected) FocusForestMintBadge else Color(0xFFEFF3F1)
    val iconTint = if (isSelected) FocusForestGreenDeep else FocusInactiveIcon

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = null,
                indication = ripple(bounded = true),
                onClick = onSelect,
            )
            .semantics {
                role = Role.RadioButton
                this.contentDescription = goal.shortLabel
            },
        shape = RoundedCornerShape(18.dp),
        color = animatedBg,
        border = BorderStroke(if (isSelected) 1.6.dp else 1.dp, animatedBorderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = goal.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(goal.titleRes),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusDarkText,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(goal.subtitleRes),
                    fontSize = 12.sp,
                    color = FocusMutedText,
                    lineHeight = 16.sp,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Selection Radio / Checkmark Indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(FocusForestGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .border(BorderStroke(1.5.dp, Color(0xFFD0D7D3)), CircleShape),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 3: Distractions Selection ("What usually breaks your focus?")
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingStep3Distractions(
    selectedDistractions: Set<DistractionType>,
    onToggleDistraction: (DistractionType) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        StepIconBadge(icon = Icons.Outlined.PhoneAndroid)

        Spacer(modifier = Modifier.height(18.dp))

        // Headline
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "What usually ",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusDarkText,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = "breaks",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusForestGreen,
                )
            }
            Text(
                text = "your focus?",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FocusDarkText,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.onboarding_s3_subtitle),
                fontSize = 14.sp,
                color = FocusMutedText,
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 5 Checkbox items
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DistractionType.entries.forEach { distraction ->
                val isChecked = selectedDistractions.contains(distraction)
                DistractionCheckboxCard(
                    distraction = distraction,
                    isChecked = isChecked,
                    onToggle = { onToggleDistraction(distraction) },
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(28.dp))

        OnboardingPrimaryButton(
            text = stringResource(R.string.onboarding_continue),
            onClick = onContinue,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DistractionCheckboxCard(
    distraction: DistractionType,
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isChecked) Color(0xFFF9FBFA) else Color.White,
        label = "distraction_bg",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isChecked) Color(0xFFD6EDE0) else Color(0xFFE5ECE8),
        label = "distraction_border",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = null,
                indication = ripple(bounded = true),
                onClick = onToggle,
            )
            .semantics {
                role = Role.Checkbox
                this.contentDescription = "${distraction.name}, checked: $isChecked"
            },
        shape = RoundedCornerShape(16.dp),
        color = animatedBg,
        border = BorderStroke(1.dp, animatedBorderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF3F1)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = distraction.icon,
                    contentDescription = null,
                    tint = if (isChecked) FocusForestGreen else FocusInactiveIcon,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = stringResource(distraction.titleRes),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = FocusDarkText,
                modifier = Modifier.weight(1f),
            )

            // Rounded Checkbox
            if (isChecked) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(FocusForestGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp),
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .border(BorderStroke(1.5.dp, Color(0xFFCCD4D0)), RoundedCornerShape(6.dp)),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 4: Daily Target / Commitment ("How much focused time do you want each day?")
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingStep4DailyTarget(
    selectedDailyTarget: DailyFocusTarget,
    onSelectDailyTarget: (DailyFocusTarget) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        StepIconBadge(icon = Icons.Outlined.Schedule)

        Spacer(modifier = Modifier.height(18.dp))

        // Headline
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "How much focused",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FocusDarkText,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = "time do you want",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FocusDarkText,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "each day",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusForestGreen,
                )
                Text(
                    text = "?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusDarkText,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.onboarding_s4_subtitle),
                fontSize = 14.sp,
                color = FocusMutedText,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2x2 Grid of Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DailyTargetCard(
                    target = DailyFocusTarget.MIN_15,
                    isSelected = selectedDailyTarget == DailyFocusTarget.MIN_15,
                    onClick = { onSelectDailyTarget(DailyFocusTarget.MIN_15) },
                    modifier = Modifier.weight(1f),
                )
                DailyTargetCard(
                    target = DailyFocusTarget.MIN_30,
                    isSelected = selectedDailyTarget == DailyFocusTarget.MIN_30,
                    onClick = { onSelectDailyTarget(DailyFocusTarget.MIN_30) },
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DailyTargetCard(
                    target = DailyFocusTarget.MIN_60,
                    isSelected = selectedDailyTarget == DailyFocusTarget.MIN_60,
                    onClick = { onSelectDailyTarget(DailyFocusTarget.MIN_60) },
                    modifier = Modifier.weight(1f),
                )
                DailyTargetCard(
                    target = DailyFocusTarget.MIN_90,
                    isSelected = selectedDailyTarget == DailyFocusTarget.MIN_90,
                    onClick = { onSelectDailyTarget(DailyFocusTarget.MIN_90) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(28.dp))

        OnboardingPrimaryButton(
            text = stringResource(R.string.onboarding_continue),
            onClick = onContinue,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DailyTargetCard(
    target: DailyFocusTarget,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) FocusForestMintBg else Color.White,
        label = "target_bg",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) FocusForestMintBorder else Color(0xFFE5ECE8),
        label = "target_border",
    )

    Surface(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = null,
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .semantics {
                role = Role.RadioButton
                this.contentDescription = target.displayLabel
            },
        shape = RoundedCornerShape(20.dp),
        color = animatedBg,
        border = BorderStroke(if (isSelected) 1.6.dp else 1.dp, animatedBorderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Radio circle on top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(FocusForestGreen),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(BorderStroke(1.5.dp, Color(0xFFD0D7D3)), CircleShape),
                    )
                }
            }

            // Text
            Column {
                Text(
                    text = stringResource(target.titleRes),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FocusDarkText,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(target.subtitleRes),
                    fontSize = 11.5.sp,
                    color = if (isSelected) FocusForestGreenDeep else FocusMutedText,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 5: Focus Plan Summary ("Your focus plan is ready 🎉")
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingStep5PlanReady(
    selectedGoal: OnboardingGoal,
    selectedDailyTarget: DailyFocusTarget,
    onStartPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        StepIconBadge(icon = Icons.Outlined.AutoAwesome)

        Spacer(modifier = Modifier.height(18.dp))

        // Headline
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Your focus plan",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FocusDarkText,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "is ready",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusForestGreen,
                )
                Text(
                    text = " 🎉",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FocusDarkText,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.onboarding_s5_subtitle),
                fontSize = 14.sp,
                color = FocusMutedText,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3-Metric Summary Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFAFBFA),
            border = BorderStroke(1.dp, FocusCardBorder),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Metric 1: Daily Target
                PlanMetricItem(
                    icon = Icons.Outlined.Schedule,
                    label = selectedDailyTarget.displayLabel,
                )

                // Metric 2: Chosen Goal
                PlanMetricItem(
                    icon = selectedGoal.icon,
                    label = selectedGoal.shortLabel,
                )

                // Metric 3: Distraction Blocking
                PlanMetricItem(
                    icon = Icons.Outlined.Block,
                    label = stringResource(R.string.onboarding_s5_metric_block),
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Mountain Trail Journey Visual
        MountainJourneyVisual(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        )

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(28.dp))

        // Action Button
        OnboardingPrimaryButton(
            text = stringResource(R.string.onboarding_start_plan),
            onClick = onStartPlan,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PlanMetricItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FocusDarkText,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = FocusDarkText,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Custom milestone mountain journey visual showing steps 1, 2, and 3 climbing to the summit.
 */
@Composable
private fun MountainJourneyVisual(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        // Mountain Backdrop & Dashed Trail Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Mountain Silhouette (Calm mint green tones)
            val mountainPath = Path().apply {
                moveTo(0f, height)
                lineTo(width * 0.2f, height * 0.65f)
                lineTo(width * 0.45f, height * 0.45f)
                lineTo(width * 0.72f, height * 0.22f) // Peak
                lineTo(width, height * 0.75f)
                lineTo(width, height)
                close()
            }
            drawPath(
                path = mountainPath,
                color = Color(0xFFF1F7F4),
            )

            // Warm golden sun on peak
            drawCircle(
                color = Color(0xFFFFECC4),
                radius = 32.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(width * 0.72f, height * 0.22f),
            )

            // Dashed trail connecting milestone steps
            val trailPath = Path().apply {
                moveTo(width * 0.16f, height * 0.75f)
                quadraticTo(
                    width * 0.35f, height * 0.72f,
                    width * 0.48f, height * 0.52f,
                )
                quadraticTo(
                    width * 0.60f, height * 0.36f,
                    width * 0.78f, height * 0.28f,
                )
            }
            drawPath(
                path = trailPath,
                color = FocusForestGreen,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                    cap = StrokeCap.Round,
                ),
            )
        }

        // Milestone Steps Row / Overlays
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            // Step 1: Build Consistency
            MilestoneStepNode(
                number = "1",
                label = stringResource(R.string.onboarding_s5_milestone_1),
                isSummit = false,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            // Step 2: Stay Focused
            MilestoneStepNode(
                number = "2",
                label = stringResource(R.string.onboarding_s5_milestone_2),
                isSummit = false,
                modifier = Modifier.padding(bottom = 44.dp),
            )

            // Step 3: See Progress (Summit Flag)
            MilestoneStepNode(
                number = "3",
                label = stringResource(R.string.onboarding_s5_milestone_3),
                isSummit = true,
                modifier = Modifier.padding(bottom = 76.dp),
            )
        }
    }
}

@Composable
private fun MilestoneStepNode(
    number: String,
    label: String,
    isSummit: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isSummit) {
            Icon(
                imageVector = Icons.Filled.Flag,
                contentDescription = null,
                tint = FocusForestGreen,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(FocusForestGreen),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = FocusDarkText,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Common Shared UI Components
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Common circular badge at the top of steps 2, 3, 4, and 5.
 */
@Composable
private fun StepIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(FocusForestMintBg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FocusForestGreen,
            modifier = Modifier.size(28.dp),
        )
    }
}

/**
 * Standard primary dark forest green pill button.
 */
@Composable
private fun OnboardingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FocusForestGreen,
            contentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Step 1 — Welcome")
@Composable
private fun OnboardingStep1Preview() {
    FocusFlowTheme(darkTheme = false) {
        OnboardingScreenContent(
            uiState = OnboardingUiState(currentStep = 1),
            onNextStep = {},
            onSkip = {},
            onSelectGoal = {},
            onToggleDistraction = {},
            onSelectDailyTarget = {},
        )
    }
}

@Preview(showBackground = true, name = "Step 2 — Goals")
@Composable
private fun OnboardingStep2Preview() {
    FocusFlowTheme(darkTheme = false) {
        OnboardingScreenContent(
            uiState = OnboardingUiState(currentStep = 2),
            onNextStep = {},
            onSkip = {},
            onSelectGoal = {},
            onToggleDistraction = {},
            onSelectDailyTarget = {},
        )
    }
}

@Preview(showBackground = true, name = "Step 3 — Distractions")
@Composable
private fun OnboardingStep3Preview() {
    FocusFlowTheme(darkTheme = false) {
        OnboardingScreenContent(
            uiState = OnboardingUiState(currentStep = 3),
            onNextStep = {},
            onSkip = {},
            onSelectGoal = {},
            onToggleDistraction = {},
            onSelectDailyTarget = {},
        )
    }
}

@Preview(showBackground = true, name = "Step 4 — Daily Target")
@Composable
private fun OnboardingStep4Preview() {
    FocusFlowTheme(darkTheme = false) {
        OnboardingScreenContent(
            uiState = OnboardingUiState(currentStep = 4),
            onNextStep = {},
            onSkip = {},
            onSelectGoal = {},
            onToggleDistraction = {},
            onSelectDailyTarget = {},
        )
    }
}

@Preview(showBackground = true, name = "Step 5 — Plan Ready")
@Composable
private fun OnboardingStep5Preview() {
    FocusFlowTheme(darkTheme = false) {
        OnboardingScreenContent(
            uiState = OnboardingUiState(currentStep = 5),
            onNextStep = {},
            onSkip = {},
            onSelectGoal = {},
            onToggleDistraction = {},
            onSelectDailyTarget = {},
        )
    }
}
