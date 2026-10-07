package com.focusflow.core.domain.permission

/**
 * Android system permissions required or optionally used by FocusFlow.
 * Pure domain representation without framework dependencies.
 */
enum class AppPermission(
    val isCore: Boolean,
) {
    /**
     * Usage Access (PACKAGE_USAGE_STATS).
     * Required for reading screen time and detecting foreground apps.
     */
    USAGE_ACCESS(isCore = true),

    /**
     * Overlay / Display over other apps (SYSTEM_ALERT_WINDOW).
     * Required to display the calm blocking overlay over restricted apps.
     */
    OVERLAY(isCore = true),

    /**
     * Notifications (POST_NOTIFICATIONS on Android 13+).
     * Required to display ongoing focus session controls and alerts.
     */
    NOTIFICATIONS(isCore = true),

    /**
     * Battery optimization exemption (REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).
     * Recommended to prevent OEM killers from terminating foreground focus sessions.
     */
    BATTERY_OPTIMIZATION(isCore = false),

    /**
     * Accessibility Service (optional module).
     * Used exclusively for YouTube Study Mode and Shorts/Reels exit signals.
     */
    ACCESSIBILITY(isCore = false),
}
