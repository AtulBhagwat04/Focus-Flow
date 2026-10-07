package com.focusflow.app.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusflow.app.R
import com.focusflow.app.system.permission.AndroidPermissionChecker
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusNavInactive
import com.focusflow.feature.home.HomeScreen
import com.focusflow.feature.limits.LimitsScreen
import com.focusflow.feature.profile.ProfileScreen
import com.focusflow.feature.rooms.RoomsScreen
import com.focusflow.feature.stats.StatsScreen
import com.focusflow.feature.stats.permission.PermissionHealthScreen
import com.focusflow.feature.timer.TimerScreen

// ─── Top-level route definitions ─────────────────────────────────────────────

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
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = ROUTE_HOME,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    TopLevelDestination(
        route = ROUTE_STATS,
        labelRes = R.string.nav_insights,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
    ),
    TopLevelDestination(
        route = ROUTE_PROFILE,
        labelRes = R.string.nav_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.PersonOutline,
    ),
)

/**
 * Root navigation host for the app.
 *
 * Implements the polished FocusFlow design with matching bottom navigation:
 * Home (with active indicator), Insights, and Profile.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, FocusCardBorder),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    topLevelDestinations.forEach { destination ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == destination.route } == true

                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = null,
                                    indication = ripple(bounded = false, radius = 28.dp),
                                ) {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                }
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = null,
                                tint = if (selected) FocusForestGreen else FocusNavInactive,
                                modifier = Modifier.size(24.dp),
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = stringResource(destination.labelRes),
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) FocusForestGreen else FocusNavInactive,
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Active indicator pill
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(FocusForestGreen),
                                )
                            } else {
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }
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
                    },
                    onNavigateToSettings = {
                        navController.navigate(ROUTE_PERMISSION_HEALTH)
                    },
                )
            }
            composable(ROUTE_FOCUS)   { TimerScreen() }
            composable(ROUTE_STATS) {
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
