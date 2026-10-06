package com.focusflow.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ─── Branded fallback color schemes ──────────────────────────────────────────
// Used on API < 31 (no dynamic color) and as the design-system reference palette.

private val LightColorScheme = lightColorScheme(
    primary          = FocusPrimary,
    secondary        = FocusSecondary,
    tertiary         = FocusTertiary,
    surface          = FocusSurface,
    surfaceVariant   = FocusSurface2,
    onSurface        = FocusOnSurface,
    onSurfaceVariant = BlockNeutral,
    error            = FocusError,
)

private val DarkColorScheme = darkColorScheme(
    primary          = FocusPrimaryDark,
    secondary        = FocusSecondaryDark,
    tertiary         = FocusTertiaryDark,
    surface          = FocusSurfaceDark,
    surfaceVariant   = FocusSurface2Dark,
    onSurface        = FocusOnSurfaceDark,
    onSurfaceVariant = BlockNeutralDark,
    error            = FocusErrorDark,
)

/**
 * Root theme composable for FocusFlow.
 *
 * - Supports Material 3 dynamic color (Monet) on API 31+ with branded fallback.
 * - Light and dark themes are first-class and follow the system setting.
 * - A manual override is available via [darkTheme] parameter (wired from Settings in M8).
 * - UI_UX_SPEC.md §2: "Both light and dark themes are first-class."
 */
@Composable
fun FocusFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = FocusFlowTypography,
        shapes      = FocusFlowShapes,
        content     = content,
    )
}
