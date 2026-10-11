package com.focusflow.feature.home.login

/**
 * Immutable UI state for the Login screen.
 * Follows unidirectional data flow rules.
 */
data class LoginUiState(
    val isGoogleLoading: Boolean = false,
    val isGuestLoading: Boolean = false,
    val errorMessage: String? = null,
    val showTermsDialog: Boolean = false,
    val showPrivacyDialog: Boolean = false,
    val isAuthenticated: Boolean = false,
)
