package com.focusflow.core.domain.permission

/**
 * State of a system permission.
 */
sealed interface PermissionState {
    /** Permission is active and granted. */
    data object Granted : PermissionState

    /** Permission is denied or not yet requested. */
    data object Denied : PermissionState

    /**
     * Android 13+ restricted settings state (sideloaded builds or system restriction).
     * The user must explicitly allow restricted settings in system App Info first.
     */
    data object Restricted : PermissionState

    /** Permission is not required on this Android API level. */
    data object NotRequired : PermissionState

    val isGranted: Boolean
        get() = this is Granted || this is NotRequired
}
