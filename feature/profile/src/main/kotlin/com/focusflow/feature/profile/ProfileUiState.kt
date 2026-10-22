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
    val weeklyFocusMinutes: Long = 750L,
    val completedSessionsCount: Int = 18,
    val weeklyGoalHours: Int = 16,
    val blockedAppsCount: Int = 77,
    val remindersEnabled: Boolean = true,
    val appearanceTheme: String = "Light",
)
