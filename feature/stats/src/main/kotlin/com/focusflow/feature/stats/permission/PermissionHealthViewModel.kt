package com.focusflow.feature.stats.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.core.domain.permission.AppPermission
import com.focusflow.core.domain.permission.PermissionHealth
import com.focusflow.core.domain.permission.PermissionHealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionHealthViewModel @Inject constructor(
    private val repository: PermissionHealthRepository,
) : ViewModel() {

    val uiState: StateFlow<PermissionHealthUiState> = repository.permissionHealth
        .map { health -> buildUiState(health) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PermissionHealthUiState(),
        )

    fun refresh() {
        viewModelScope.launch {
            repository.refreshHealth()
        }
    }

    private fun buildUiState(health: PermissionHealth): PermissionHealthUiState {
        val items = AppPermission.entries.map { permission ->
            val (title, description) = when (permission) {
                AppPermission.USAGE_ACCESS -> "Usage Access" to
                    "Required to read daily screen time and detect foreground apps for blocking."
                AppPermission.OVERLAY -> "Display Over Other Apps" to
                    "Required to show the calm focus overlay when opening a restricted app."
                AppPermission.NOTIFICATIONS -> "Notifications" to
                    "Required for ongoing focus session controls and break reminders."
                AppPermission.BATTERY_OPTIMIZATION -> "Battery Exemption" to
                    "Recommended to protect focus sessions from aggressive system kills."
                AppPermission.ACCESSIBILITY -> "Study Mode & Reels Signal (Optional)" to
                    "Feeds extra signals for YouTube Study Mode and short-form video blocking."
            }
            PermissionUiItem(
                permission = permission,
                title = title,
                description = description,
                state = health.stateOf(permission),
                isCore = permission.isCore,
            )
        }

        return PermissionHealthUiState(
            healthPercentage = health.healthPercentage,
            isCoreOperational = health.isCoreOperational,
            items = items,
        )
    }
}
