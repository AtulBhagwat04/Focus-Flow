package com.focusflow.app.system.permission

import android.os.Build
import com.focusflow.core.domain.permission.OemBatteryGuide

/**
 * Provides OEM-specific instructions to protect FocusFlow from aggressive background termination.
 * Based on known manufacturer background policies (Samsung, Xiaomi, OnePlus/Oppo, Huawei, Pixel).
 */
object OemBatteryGuideProvider {

    /**
     * Resolves the current device's battery guide based on [Build.MANUFACTURER].
     */
    fun getGuideForCurrentDevice(): OemBatteryGuide {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("samsung") -> OemBatteryGuide(
                manufacturerName = "Samsung",
                title = "Samsung One UI Battery Settings",
                steps = listOf(
                    "Open Settings > Apps > FocusFlow > Battery.",
                    "Select 'Unrestricted' instead of 'Optimized'.",
                    "In Settings > Device care > Battery > Background usage limits, " +
                        "add FocusFlow to 'Never auto-sleeping apps'.",
                ),
            )
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
                OemBatteryGuide(
                    manufacturerName = "Xiaomi / HyperOS / MIUI",
                    title = "Xiaomi Autostart & Battery Saver",
                    steps = listOf(
                        "Open Settings > Apps > Manage apps > FocusFlow.",
                        "Enable 'Autostart'.",
                        "Tap 'Battery saver' and select 'No restrictions'.",
                    ),
                )
            }
            manufacturer.contains("oneplus") || manufacturer.contains("oppo") || manufacturer.contains("realme") -> {
                OemBatteryGuide(
                    manufacturerName = "OnePlus / OPPO / Realme",
                    title = "Battery & App Management",
                    steps = listOf(
                        "Open Settings > Battery > More settings > App battery management.",
                        "Find FocusFlow and allow foreground & background activity.",
                        "Disable 'Auto-freeze' or 'Optimize battery use' for FocusFlow.",
                    ),
                )
            }
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> OemBatteryGuide(
                manufacturerName = "Huawei / Honor",
                title = "App Launch Protection",
                steps = listOf(
                    "Open Settings > Battery > App launch.",
                    "Find FocusFlow and toggle from 'Manage automatically' to 'Manage manually'.",
                    "Enable 'Auto-launch', 'Secondary launch', and 'Run in background'.",
                ),
            )
            else -> OemBatteryGuide(
                manufacturerName = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
                title = "Standard Android Battery Optimization",
                steps = listOf(
                    "Open App Info > Battery or App battery usage.",
                    "Set battery usage to 'Unrestricted'.",
                ),
            )
        }
    }
}
