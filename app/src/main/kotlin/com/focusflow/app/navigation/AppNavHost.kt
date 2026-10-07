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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.focusflow.app.R
import com.focusflow.app.system.permission.AndroidPermissionChecker
import com.focusflow.feature.home.HomeScreen
import com.focusflow.feature.limits.LimitsScreen
import com.focusflow.feature.profile.ProfileScreen
import com.focusflow.feature.rooms.RoomsScreen
import com.focusflow.feature.stats.StatsScreen
import com.focusflow.feature.stats.permission.PermissionHealthScreen
import com.focusflow.feature.timer.TimerScreen

// ─── Top-level route definitions ─────────────────────────────────────────────
// Using string routes for now; will migrate to type-safe serializable objects
// in M2 once Navigation Compose 2.9 type-safety is adopted project-wide.

internal const val ROUTE_HOME              = "home"
internal const val ROUTE_FOCUS             = "focus"
internal const val ROUTE_STATS             = "stats"
internal const val ROUTE_ROOMS             = "rooms"
internal const val ROUTE_PROFILE           = "profile"
internal const val ROUTE_PERMISSION_HEALTH = "permission_health"
internal const val ROUTE_LIMITS            = "limits"

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: @Composable () -> Unit,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = ROUTE_HOME,
        labelRes = R.string.nav_home,
        icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
    ),
    TopLevelDestination(
        route = ROUTE_FOCUS,
        labelRes = R.string.nav_focus,
        icon = { Icon(Icons.Outlined.Timer, contentDescription = null) },
    ),
    TopLevelDestination(
        route = ROUTE_STATS,
        labelRes = R.string.nav_stats,
        icon = { Icon(Icons.Outlined.BarChart, contentDescription = null) },
    ),
    TopLevelDestination(
        route = ROUTE_ROOMS,
        labelRes = R.string.nav_rooms,
        icon = { Icon(Icons.Outlined.Groups, contentDescription = null) },
    ),
    TopLevelDestination(
        route = ROUTE_PROFILE,
        labelRes = R.string.nav_profile,
        icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
    ),
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
            composable(ROUTE_HOME) {
                HomeScreen(
                    onNavigateToFocus = {
                        navController.navigate(ROUTE_FOCUS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToLimits = {
                        navController.navigate(ROUTE_LIMITS)
                    }
                )
            }
            composable(ROUTE_FOCUS)   { TimerScreen() }
            composable(ROUTE_STATS) {
                val context = LocalContext.current
                StatsScreen(
                    onNavigateToPermissions = {
                        navController.navigate(ROUTE_PERMISSION_HEALTH)
                    }
                )
            }
            composable(ROUTE_PERMISSION_HEALTH) {
                val context = LocalContext.current
                val checker = remember { AndroidPermissionChecker(context.applicationContext) }
                PermissionHealthScreen(
                    onOpenPermissionSettings = { permission ->
                        try {
                            context.startActivity(checker.createSettingIntent(permission))
                        } catch (_: Exception) {
                            context.startActivity(checker.createAppDetailsIntent())
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(ROUTE_LIMITS)  { LimitsScreen() }
            composable(ROUTE_ROOMS)   { RoomsScreen() }
            composable(ROUTE_PROFILE) { ProfileScreen() }
        }
    }
}
