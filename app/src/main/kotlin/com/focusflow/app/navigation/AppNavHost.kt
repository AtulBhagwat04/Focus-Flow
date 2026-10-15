package com.focusflow.app.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
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
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusNavInactive
import com.focusflow.feature.home.HomeScreen
import com.focusflow.feature.home.login.LoginScreen
import com.focusflow.feature.home.onboarding.OnboardingScreen
import com.focusflow.feature.home.tasks.TasksScreen
import com.focusflow.feature.limits.LimitsScreen
import com.focusflow.feature.profile.ProfileScreen
import com.focusflow.feature.rooms.RoomsScreen
import com.focusflow.feature.stats.StatsScreen
import com.focusflow.feature.stats.permission.PermissionHealthScreen
import com.focusflow.feature.timer.TimerScreen

// ─── Top-level route definitions ─────────────────────────────────────────────

internal const val ROUTE_LOGIN             = "login"
internal const val ROUTE_ONBOARDING        = "onboarding"
internal const val ROUTE_HOME              = "home"
internal const val ROUTE_FOCUS             = "focus"
internal const val ROUTE_TASKS             = "tasks"
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
        route = ROUTE_FOCUS,
        labelRes = R.string.nav_focus,
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer,
    ),
    TopLevelDestination(
        route = ROUTE_TASKS,
        labelRes = R.string.nav_tasks,
        selectedIcon = Icons.AutoMirrored.Filled.Assignment,
        unselectedIcon = Icons.AutoMirrored.Outlined.Assignment,
    ),
    TopLevelDestination(
        route = ROUTE_STATS,
        labelRes = R.string.nav_stats,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
    ),
)

/**
 * Root navigation host for the app.
 *
 * Implements the polished FocusFlow design with matching bottom navigation:
 * Home (with active indicator), Insights, and Profile.
 */
@Composable
fun AppNavHost(
    startDestination: String = ROUTE_LOGIN,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val isFullscreenRoute = currentRoute == ROUTE_LOGIN || currentRoute == ROUTE_ONBOARDING
    val isScenicEdgeToEdgeRoute = isFullscreenRoute || currentRoute == ROUTE_HOME || currentRoute == ROUTE_FOCUS
    val showBottomBar = !isFullscreenRoute

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, FocusCardBorder),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        topLevelDestinations.forEach { destination ->
                            val selected = currentDestination?.hierarchy
                                ?.any { it.route == destination.route } == true

                            Column(
                                modifier = Modifier
                                    .weight(1f)
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
                                    .padding(vertical = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (selected) Color(0xFFD6EFE1) else Color.Transparent)
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = null,
                                        tint = if (selected) FocusForestGreenDeep else FocusNavInactive,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = stringResource(destination.labelRes),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) FocusForestGreenDeep else FocusNavInactive,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = startDestination,
            modifier         = if (isScenicEdgeToEdgeRoute) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding(),
                    )
            },
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(ROUTE_ONBOARDING) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            composable(ROUTE_ONBOARDING) {
                OnboardingScreen(
                    onCompleteOnboarding = {
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
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
                    onNavigateToProfile = {
                        navController.navigate(ROUTE_PROFILE)
                    },
                    onNavigateToStats = {
                        navController.navigate(ROUTE_STATS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToTasks = {
                        navController.navigate(ROUTE_TASKS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(ROUTE_FOCUS) {
                TimerScreen(
                    onNavigateToLimits = {
                        navController.navigate(ROUTE_LIMITS)
                    },
                    onNavigateToSettings = {
                        navController.navigate(ROUTE_PERMISSION_HEALTH)
                    },
                )
            }
            composable(ROUTE_TASKS) {
                TasksScreen(
                    onStartFocusOnTask = { _, _ ->
                        navController.navigate(ROUTE_FOCUS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
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
