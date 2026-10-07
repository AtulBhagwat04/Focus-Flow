package com.focusflow.app.system.permission

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.focusflow.core.domain.permission.AppPermission
import com.focusflow.core.domain.permission.PermissionState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation for checking system permission states.
 * Handles Android 8 through 16/17 compatibility.
 */
@Singleton
class AndroidPermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Checks the current state of a given [AppPermission].
     */
    fun checkPermission(permission: AppPermission): PermissionState {
        return when (permission) {
            AppPermission.USAGE_ACCESS -> checkUsageAccess()
            AppPermission.OVERLAY -> checkOverlay()
            AppPermission.NOTIFICATIONS -> checkNotifications()
            AppPermission.BATTERY_OPTIMIZATION -> checkBatteryOptimization()
            AppPermission.ACCESSIBILITY -> checkAccessibility()
        }
    }

    /**
     * Checks all permissions and returns a mapped result.
     */
    fun checkAll(): Map<AppPermission, PermissionState> {
        return AppPermission.entries.associateWith { checkPermission(it) }
    }

    /**
     * Creates an Intent to navigate the user directly to the relevant system settings screen.
     */
    fun createSettingIntent(permission: AppPermission): Intent {
        return when (permission) {
            AppPermission.USAGE_ACCESS -> Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            AppPermission.OVERLAY -> Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            AppPermission.NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                } else {
                    createAppDetailsIntent()
                }
            }
            AppPermission.BATTERY_OPTIMIZATION -> {
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            AppPermission.ACCESSIBILITY -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    /**
     * Creates an Intent to open the app's system App Info page.
     */
    fun createAppDetailsIntent(): Intent {
        return Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    private fun checkUsageAccess(): PermissionState {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return PermissionState.Denied
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }
        return if (mode == AppOpsManager.MODE_ALLOWED) PermissionState.Granted else PermissionState.Denied
    }

    private fun checkOverlay(): PermissionState {
        return if (Settings.canDrawOverlays(context)) {
            PermissionState.Granted
        } else {
            PermissionState.Denied
        }
    }

    private fun checkNotifications(): PermissionState {
        return if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            PermissionState.Granted
        } else {
            PermissionState.Denied
        }
    }

    private fun checkBatteryOptimization(): PermissionState {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: return PermissionState.NotRequired
        return if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            PermissionState.Granted
        } else {
            PermissionState.Denied
        }
    }

    private fun checkAccessibility(): PermissionState {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return PermissionState.Denied

        val expectedService = "${context.packageName}/"
        return if (enabledServices.contains(expectedService)) {
            PermissionState.Granted
        } else {
            PermissionState.Denied
        }
    }
}
