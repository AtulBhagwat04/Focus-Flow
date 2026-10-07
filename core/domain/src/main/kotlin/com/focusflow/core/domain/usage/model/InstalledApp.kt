package com.focusflow.core.domain.usage.model

/**
 * Represents an installed launchable application on the user's device.
 */
data class InstalledApp(
    val packageName: String,
    val label: String,
    val isSafeListed: Boolean,
    val isEssential: Boolean = false,
    val category: AppCategory = AppCategory.OTHER,
)
