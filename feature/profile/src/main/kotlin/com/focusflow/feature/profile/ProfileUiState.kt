package com.focusflow.feature.profile

import com.focusflow.core.domain.auth.model.UserAccount
import com.focusflow.core.domain.auth.model.UserProfile

data class ProfileUiState(
    val account: UserAccount? = null,
    val profile: UserProfile? = null,
    val isSyncing: Boolean = false,
    val exportedDataJson: String? = null,
    val isExporting: Boolean = false,
    val message: String? = null,
    val isLoading: Boolean = false,
    val weeklyFocusMinutes: Long = 0L,
    val weeklyFocusFormatted: String = "0m",
    val completedSessionsCount: Int = 0,
    val currentStreakDays: Int = 0,
    val level: Int = 1,
    val xp: Long = 0L,
    val weeklyGoalHours: Int = 16,
    val blockedAppsCount: Int = 0,
    val remindersEnabled: Boolean = true,
    val appearanceTheme: String = "Light",
)
