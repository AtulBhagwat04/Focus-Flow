package com.focusflow.feature.profile

/** Bundles all user-initiated event callbacks for [ProfileScreen]. */
data class ProfileActions(
    val onSync: () -> Unit,
    val onExport: () -> Unit,
    val onSignOut: () -> Unit,
    val onDeleteRequested: () -> Unit,
    val onNavigateToLimits: () -> Unit,
    val onNavigateToPermissionHealth: () -> Unit,
)
