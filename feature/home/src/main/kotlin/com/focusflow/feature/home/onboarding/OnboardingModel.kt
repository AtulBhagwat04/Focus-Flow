package com.focusflow.feature.home.onboarding

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector
import com.focusflow.feature.home.R

/**
 * Main goal choices for Step 2 of Onboarding.
 */
enum class OnboardingGoal(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector,
    val shortLabel: String,
) {
    STUDY(
        titleRes = R.string.onboarding_goal_study_title,
        subtitleRes = R.string.onboarding_goal_study_subtitle,
        icon = Icons.Outlined.School,
        shortLabel = "Study",
    ),
    WORK(
        titleRes = R.string.onboarding_goal_work_title,
        subtitleRes = R.string.onboarding_goal_work_subtitle,
        icon = Icons.Outlined.Laptop,
        shortLabel = "Work",
    ),
    HABITS(
        titleRes = R.string.onboarding_goal_habits_title,
        subtitleRes = R.string.onboarding_goal_habits_subtitle,
        icon = Icons.Outlined.Eco,
        shortLabel = "Habits",
    ),
    SCREEN_TIME(
        titleRes = R.string.onboarding_goal_screen_time_title,
        subtitleRes = R.string.onboarding_goal_screen_time_subtitle,
        icon = Icons.Outlined.BarChart,
        shortLabel = "Screen Time",
    ),
}

/**
 * Distraction categories for Step 3 of Onboarding.
 */
enum class DistractionType(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
) {
    SOCIAL_MEDIA(
        titleRes = R.string.onboarding_distraction_social,
        icon = Icons.Outlined.CameraAlt,
    ),
    YOUTUBE(
        titleRes = R.string.onboarding_distraction_youtube,
        icon = Icons.Outlined.PlayCircle,
    ),
    GAMES(
        titleRes = R.string.onboarding_distraction_games,
        icon = Icons.Outlined.SportsEsports,
    ),
    MESSAGES(
        titleRes = R.string.onboarding_distraction_messages,
        icon = Icons.AutoMirrored.Outlined.Chat,
    ),
    EVERYTHING(
        titleRes = R.string.onboarding_distraction_everything,
        icon = Icons.Outlined.MoreHoriz,
    ),
}

/**
 * Daily target options for Step 4 of Onboarding.
 */
enum class DailyFocusTarget(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val minutes: Int,
    val displayLabel: String,
) {
    MIN_15(
        titleRes = R.string.onboarding_time_15_title,
        subtitleRes = R.string.onboarding_time_15_subtitle,
        minutes = 15,
        displayLabel = "15 min/day",
    ),
    MIN_30(
        titleRes = R.string.onboarding_time_30_title,
        subtitleRes = R.string.onboarding_time_30_subtitle,
        minutes = 30,
        displayLabel = "30 min/day",
    ),
    MIN_60(
        titleRes = R.string.onboarding_time_60_title,
        subtitleRes = R.string.onboarding_time_60_subtitle,
        minutes = 60,
        displayLabel = "60 min/day",
    ),
    MIN_90(
        titleRes = R.string.onboarding_time_90_title,
        subtitleRes = R.string.onboarding_time_90_subtitle,
        minutes = 90,
        displayLabel = "90+ min/day",
    ),
}

/**
 * Immutable UI state for the complete onboarding funnel.
 */
data class OnboardingUiState(
    val currentStep: Int = 1,
    val totalSteps: Int = 5,
    val selectedGoal: OnboardingGoal = OnboardingGoal.STUDY,
    val selectedDistractions: Set<DistractionType> = setOf(
        DistractionType.SOCIAL_MEDIA,
        DistractionType.YOUTUBE,
        DistractionType.MESSAGES,
    ),
    val selectedDailyTarget: DailyFocusTarget = DailyFocusTarget.MIN_15,
    val isCompleted: Boolean = false,
)
