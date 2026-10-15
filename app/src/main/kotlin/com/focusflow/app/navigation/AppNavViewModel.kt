package com.focusflow.app.navigation

import androidx.lifecycle.ViewModel
import com.focusflow.core.domain.auth.model.UserAccount
import com.focusflow.core.domain.auth.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AppNavViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    val isUserSignedInAtStartup: Boolean = authRepository.currentUser.value != null

    val currentUser: StateFlow<UserAccount?> = authRepository.currentUser
}

