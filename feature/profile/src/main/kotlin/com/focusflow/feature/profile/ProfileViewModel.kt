package com.focusflow.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.domain.auth.model.UserAccount
import com.focusflow.core.domain.auth.model.UserProfile
import com.focusflow.core.domain.auth.repository.AuthRepository
import com.focusflow.core.domain.blocking.repository.BlockRuleRepository
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

private const val DEFAULT_WEEKLY_MINUTES = 750L
private const val DEFAULT_SESSIONS_COUNT = 18
private const val DEFAULT_WEEKLY_GOAL_HOURS = 16
private const val DEFAULT_BLOCKED_APPS = 77

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val blockRuleRepository: BlockRuleRepository,
) : ViewModel() {

    private val exportedData = MutableStateFlow<String?>(null)
    private val isExporting = MutableStateFlow(false)
    private val userMessage = MutableStateFlow<String?>(null)
    private val remindersEnabled = MutableStateFlow(true)
    private val appearanceTheme = MutableStateFlow("Light")
    private val weeklyGoalHours = MutableStateFlow(DEFAULT_WEEKLY_GOAL_HOURS)
    private val customDisplayName = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfileUiState> = combine(
        combine(
            authRepository.currentUser,
            authRepository.userProfile,
            syncRepository.isSyncing,
            exportedData,
            isExporting,
        ) { user, profile, syncing, exportJson, exporting ->
            ProfileBaseData(user, profile, syncing, exportJson, exporting)
        },
        combine(
            userMessage,
            remindersEnabled,
            appearanceTheme,
            weeklyGoalHours,
            blockRuleRepository.observeAllRules(),
        ) { message, reminders, appearance, goal, rules ->
            ProfileSettingsData(message, reminders, appearance, goal, rules.size)
        },
        customDisplayName,
    ) { base, settings, customName ->
        val effectiveAccount = if (customName != null && base.user != null) {
            base.user.copy(displayName = customName)
        } else if (customName != null) {
            UserAccount(uid = "local", displayName = customName, isAnonymous = false)
        } else {
            base.user
        }

        ProfileUiState(
            account = effectiveAccount,
            profile = base.profile,
            isSyncing = base.syncing,
            exportedDataJson = base.exportJson,
            isExporting = base.exporting,
            message = settings.message,
            isLoading = false,
            weeklyFocusMinutes = base.profile?.totalFocusMinutes?.takeIf { it > 0 } ?: DEFAULT_WEEKLY_MINUTES,
            completedSessionsCount = DEFAULT_SESSIONS_COUNT,
            weeklyGoalHours = settings.goal,
            blockedAppsCount = if (settings.rulesCount > 0) settings.rulesCount else DEFAULT_BLOCKED_APPS,
            remindersEnabled = settings.reminders,
            appearanceTheme = settings.appearance,
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

    fun onToggleReminders(enabled: Boolean) {
        remindersEnabled.value = enabled
    }

    fun onSelectAppearance(theme: String) {
        appearanceTheme.value = theme
    }

    fun onUpdateWeeklyGoal(hours: Int) {
        weeklyGoalHours.value = hours
    }

    fun onUpdateDisplayName(name: String) {
        customDisplayName.value = name
        userMessage.value = "Name updated."
    }
}

private data class ProfileBaseData(
    val user: UserAccount?,
    val profile: UserProfile?,
    val syncing: Boolean,
    val exportJson: String?,
    val exporting: Boolean,
)

private data class ProfileSettingsData(
    val message: String?,
    val reminders: Boolean,
    val appearance: String,
    val goal: Int,
    val rulesCount: Int,
)
