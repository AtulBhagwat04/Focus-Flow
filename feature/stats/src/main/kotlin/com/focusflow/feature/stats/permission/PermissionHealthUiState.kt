package com.focusflow.feature.stats.permission

import com.focusflow.core.domain.permission.AppPermission
import com.focusflow.core.domain.permission.PermissionState

data class PermissionUiItem(
    val permission: AppPermission,
    val title: String,
    val description: String,
    val state: PermissionState,
    val isCore: Boolean,
)

data class PermissionHealthUiState(
    val healthPercentage: Int = 0,
    val isCoreOperational: Boolean = false,
    val items: List<PermissionUiItem> = emptyList(),
    val oemGuideTitle: String = "",
    val oemGuideSteps: List<String> = emptyList(),
)
