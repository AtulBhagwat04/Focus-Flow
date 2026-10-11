package com.focusflow.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.focusflow.app.navigation.AppNavHost
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity host for the entire app.
 *
 * Design decisions:
 * - [enableEdgeToEdge] configured with transparent light system bars (dark icons).
 * - Disables navigation/status bar contrast enforcement on Android 10+ so no black strip appears.
 * - [FocusFlowTheme] wraps everything so all screens inherit the design system.
 * - Navigation lives in [AppNavHost] — the activity has zero routing logic.
 * - @AndroidEntryPoint enables Hilt injection in this activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge with transparent system bars and dark icons matching light themes
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )

        // Prevent Android 10+ from drawing dark scrim behind 3-button or gesture navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }

        // Allow drawing into display cutout (notch/camera cutout) on API 28+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        setContent {
            FocusFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
