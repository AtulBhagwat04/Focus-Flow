package com.focusflow.core.domain.permission

/**
 * Aggregated health status of all permissions for FocusFlow.
 */
data class PermissionHealth(
    val statuses: Map<AppPermission, PermissionState> = emptyMap(),
) {
    /**
     * Core functionality is operational if all core permissions (Usage, Overlay, Notifications) are granted.
     */
    val isCoreOperational: Boolean
        get() = AppPermission.entries
            .filter { it.isCore }
            .all { statuses[it]?.isGranted == true }

    /**
     * Overall health percentage (0 to 100).
     */
    val healthPercentage: Int
        get() {
            if (statuses.isEmpty()) return 0
            val grantedCount = statuses.values.count { it.isGranted }
            return (grantedCount * 100) / statuses.size
        }

    fun stateOf(permission: AppPermission): PermissionState =
        statuses[permission] ?: PermissionState.Denied
}
