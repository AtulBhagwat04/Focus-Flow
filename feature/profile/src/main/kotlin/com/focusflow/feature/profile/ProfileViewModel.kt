package com.focusflow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.sync.model.SyncResult
import com.focusflow.core.domain.sync.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {

    private val exportedData = MutableStateFlow<String?>(null)
    private val isExporting = MutableStateFlow(false)
    private val userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfileUiState> = combine(
        authRepository.currentUser,
        authRepository.userProfile,
        syncRepository.isSyncing,
        exportedData,
        isExporting,
        userMessage,
    ) { account, profile, syncing, exported, exporting, message ->
        ProfileUiState(
            account = account,
            profile = profile,
            isSyncing = syncing,
            exportedDataJson = exported,
            isExporting = exporting,
            message = message,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ProfileUiState(isLoading = true),
    )

    init {
        // Auto-initialize anonymous account if no user is signed in
        viewModelScope.launch {
            authRepository.signInAnonymously()
        }
    }

    fun onSyncClicked() {
        viewModelScope.launch {
            userMessage.value = "Starting sync..."
            when (val result = syncRepository.performSync()) {
                is SyncResult.Success -> userMessage.value = "Sync completed successfully."
                is SyncResult.Failure -> userMessage.value = "Sync failed: ${result.errorMessage}"
            }
        }
    }

    fun onExportDataClicked() {
        viewModelScope.launch {
            isExporting.value = true
            syncRepository.exportUserDataJson().fold(
                onSuccess = { json ->
                    exportedData.value = json
                    userMessage.value = "Data export generated."
                },
                onFailure = { error ->
                    userMessage.value = "Export failed: ${error.message}"
                },
            )
            isExporting.value = false
        }
    }

    fun onDismissExport() {
        exportedData.value = null
    }

    fun onSignOutClicked() {
        viewModelScope.launch {
            authRepository.signOut()
            authRepository.signInAnonymously()
        }
    }

    fun onDeleteAccountClicked() {
        viewModelScope.launch {
            authRepository.deleteAccount().fold(
                onSuccess = {
                    userMessage.value = "Account and data deleted."
                    authRepository.signInAnonymously()
                },
                onFailure = { error ->
                    userMessage.value = "Deletion failed: ${error.message}"
                },
            )
        }
    }

    fun onClearMessage() {
        userMessage.value = null
    }
}
