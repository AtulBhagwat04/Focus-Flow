package com.focusflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.focusflow.app.navigation.AppNavHost
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity host for the entire app.
 *
 * Design decisions:
 * - [enableEdgeToEdge] is required for targetSdk 36; cannot be opted out on Android 15+.
 * - [FocusFlowTheme] wraps everything so all screens inherit the design system.
 * - Navigation lives in [AppNavHost] — the activity has zero routing logic.
 * - @AndroidEntryPoint enables Hilt injection in this activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Required for API 35+. Also handles status/nav bar colours automatically.
        enableEdgeToEdge()

        setContent {
            FocusFlowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
    }
}
