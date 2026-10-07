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
)
