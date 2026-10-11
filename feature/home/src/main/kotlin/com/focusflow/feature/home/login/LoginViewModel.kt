package com.focusflow.feature.home.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.domain.auth.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoginEffect {
    data object NavigateForward : LoginEffect
}

/**
 * ViewModel managing the Login screen state and Google / Guest authentication.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effects = Channel<LoginEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onGoogleSignInStarted() {
        _uiState.update { it.copy(isGoogleLoading = true, errorMessage = null) }
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true, errorMessage = null) }
            val result = authRepository.signInWithGoogle(idToken)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isGoogleLoading = false, isAuthenticated = true) }
                    _effects.send(LoginEffect.NavigateForward)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            errorMessage = error.localizedMessage ?: "Unable to complete Google sign in."
                        )
                    }
                }
            )
        }
    }

    fun onGoogleSignInError(error: Throwable) {
        val message = if (error is UserCancelledLoginException) {
            null // User cancelled voluntarily, do not show disruptive error
        } else {
            error.localizedMessage ?: "Google sign in was unsuccessful."
        }
        _uiState.update { it.copy(isGoogleLoading = false, errorMessage = message) }
    }

    fun onGuestSignIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGuestLoading = true, errorMessage = null) }
            val result = authRepository.signInAnonymously()
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isGuestLoading = false, isAuthenticated = true) }
                    _effects.send(LoginEffect.NavigateForward)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGuestLoading = false,
                            errorMessage = error.localizedMessage ?: "Guest sign in failed."
                        )
                    }
                }
            )
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onShowTerms() {
        _uiState.update { it.copy(showTermsDialog = true) }
    }

    fun onDismissTerms() {
        _uiState.update { it.copy(showTermsDialog = false) }
    }

    fun onShowPrivacy() {
        _uiState.update { it.copy(showPrivacyDialog = true) }
    }

    fun onDismissPrivacy() {
        _uiState.update { it.copy(showPrivacyDialog = false) }
    }
}
