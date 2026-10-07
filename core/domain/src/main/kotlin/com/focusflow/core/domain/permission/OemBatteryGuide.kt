package com.focusflow.core.domain.permission

/**
 * Model describing OEM-specific battery management steps and recommendations.
 */
data class OemBatteryGuide(
    val manufacturerName: String,
    val title: String,
    val steps: List<String>,
    val settingsAction: String? = null,
)
