package com.focusflow.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.domain.auth.model.UserAccount
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.model.FocusTask
import com.focusflow.core.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppNavViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {
    val isUserSignedInAtStartup: Boolean = authRepository.currentUser.value != null

    val currentUser: StateFlow<UserAccount?> = authRepository.currentUser

    val activeTask: StateFlow<FocusTask?> = taskRepository.activeTask.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )
}

