package com.focusflow.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusflow.app.R
import com.focusflow.feature.home.HomeScreen

// ─── Top-level route definitions ─────────────────────────────────────────────
// Using string routes for now; will migrate to type-safe serializable objects
// in M2 once Navigation Compose 2.9 type-safety is adopted project-wide.

internal const val ROUTE_HOME    = "home"
internal const val ROUTE_FOCUS   = "focus"
internal const val ROUTE_STATS   = "stats"
internal const val ROUTE_ROOMS   = "rooms"
internal const val ROUTE_PROFILE = "profile"

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: @Composable () -> Unit,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(ROUTE_HOME,    R.string.nav_home,    { Icon(Icons.Outlined.Home,    contentDescription = null) }),
    TopLevelDestination(ROUTE_FOCUS,   R.string.nav_focus,   { Icon(Icons.Outlined.Timer,   contentDescription = null) }),
    TopLevelDestination(ROUTE_STATS,   R.string.nav_stats,   { Icon(Icons.Outlined.BarChart, contentDescription = null) }),
    TopLevelDestination(ROUTE_ROOMS,   R.string.nav_rooms,   { Icon(Icons.Outlined.Groups,  contentDescription = null) }),
    TopLevelDestination(ROUTE_PROFILE, R.string.nav_profile, { Icon(Icons.Outlined.Person,  contentDescription = null) }),
)

/**
 * Root navigation host for the app.
 *
 * Architecture: single NavHost with a bottom NavigationBar.
 * Adaptive rail layout (for tablets/foldables) is deferred to M3 when
 * WindowSizeClass is integrated. See UI_UX_SPEC.md §9 Adaptive layout.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                topLevelDestinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.route == destination.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick  = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon  = destination.icon,
                        label = { Text(stringResource(destination.labelRes)) },
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = ROUTE_HOME,
            modifier         = Modifier.padding(innerPadding),
        ) {
            composable(ROUTE_HOME)    { HomeScreen() }
            composable(ROUTE_FOCUS)   { /* M2: FocusScreen() */ }
            composable(ROUTE_STATS)   { /* M3: StatsScreen() */ }
            composable(ROUTE_ROOMS)   { /* M6: RoomsScreen() */ }
            composable(ROUTE_PROFILE) { /* M5: ProfileScreen() */ }
        }
    }
}
