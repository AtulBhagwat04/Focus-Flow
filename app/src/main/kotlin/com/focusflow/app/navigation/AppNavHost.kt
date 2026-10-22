package com.focusflow.app.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusflow.app.R
import com.focusflow.app.system.permission.AndroidPermissionChecker
import com.focusflow.core.designsystem.theme.FocusCardBorder
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
 * Auth-aware start destination:
 * - If Firebase already has a signed-in user (token persisted on disk), we jump
 *   straight to [ROUTE_HOME] so the user never sees the Login screen again after
 *   their first sign-in.
 * - A [LaunchedEffect] watches [AppNavViewModel.currentUser]: if the user signs out
 *   or the token is revoked at runtime, it navigates back to [ROUTE_LOGIN] and
 *   clears the entire back stack.
 *
 * FirebaseAuth persists its auth token in local storage automatically — we only
 * need to read [AppNavViewModel.isUserSignedInAtStartup] (which checks
 * FirebaseAuth.getInstance().currentUser synchronously) to decide the initial route.
 */
@Composable
fun AppNavHost(
    viewModel: AppNavViewModel = hiltViewModel(),
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val activeTask by viewModel.activeTask.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    val isUserSignedInAtStartup = remember { viewModel.isUserSignedInAtStartup }
    var hasBeenSignedIn by remember { mutableStateOf(isUserSignedInAtStartup) }

    // Synchronous check — no network call, reads the locally cached token.
    val startDestination = if (isUserSignedInAtStartup) ROUTE_HOME else ROUTE_LOGIN

    // React to runtime sign-out only if user was previously signed in
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            hasBeenSignedIn = true
        } else if (hasBeenSignedIn) {
            hasBeenSignedIn = false
            navController.navigate(ROUTE_LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val isFullscreenRoute = currentRoute == ROUTE_LOGIN || currentRoute == ROUTE_ONBOARDING
    val isScenicEdgeToEdgeRoute =
        isFullscreenRoute || currentRoute == ROUTE_HOME || currentRoute == ROUTE_FOCUS || currentRoute == ROUTE_PROFILE
    val isFocusingByTask = currentRoute == ROUTE_FOCUS && activeTask != null
    val showBottomBar = !isFullscreenRoute &&
        currentRoute != ROUTE_LIMITS &&
        currentRoute != ROUTE_PROFILE &&
        !isFocusingByTask

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                FocusFlowBottomBar(
                    destinations = topLevelDestinations,
                    currentDestination = currentDestination,
                    onNavigateToDestination = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = startDestination,
            modifier         = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (isScenicEdgeToEdgeRoute) 0.dp else innerPadding.calculateBottomPadding(),
                ),
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        hasBeenSignedIn = true
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
            composable(ROUTE_ONBOARDING) {
                OnboardingScreen(
                    onCompleteOnboarding = {
                        hasBeenSignedIn = true
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
            composable(ROUTE_HOME) {
                val context = LocalContext.current
                BackHandler {
                    (context as? Activity)?.finish()
                }
                HomeScreen(
                    onNavigateToFocus = {
                        navController.navigate(ROUTE_FOCUS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    onNavigateToLimits  = { navController.navigate(ROUTE_LIMITS) },
                    onNavigateToSettings = { navController.navigate(ROUTE_PERMISSION_HEALTH) },
                    onNavigateToProfile  = { navController.navigate(ROUTE_PROFILE) },
                    onNavigateToStats = {
                        navController.navigate(ROUTE_STATS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    onNavigateToTasks = {
                        navController.navigate(ROUTE_TASKS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                )
            }
            composable(ROUTE_FOCUS) {
                TimerScreen(
                    onNavigateToLimits   = { navController.navigate(ROUTE_LIMITS) },
                    onNavigateToSettings = { navController.navigate(ROUTE_PERMISSION_HEALTH) },
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
                            restoreState    = true
                        }
                    },
                )
            }
            composable(ROUTE_STATS) {
                StatsScreen(
                    onNavigateToPermissions = { navController.navigate(ROUTE_PERMISSION_HEALTH) },
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
                    onBack = { navController.popBackStack() },
                )
            }
            composable(ROUTE_LIMITS) {
                LimitsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(ROUTE_ROOMS)   { RoomsScreen() }
            composable(ROUTE_PROFILE) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToLimits = { navController.navigate(ROUTE_LIMITS) },
                    onNavigateToPermissionHealth = {
                        navController.navigate(ROUTE_PERMISSION_HEALTH)
                    },
                )
            }
        }
    }
}

// ─── Custom Bottom Navigation Bar ────────────────────────────────────────────

/**
 * Bottom navigation bar matching the design reference:
 * - Docked to bottom with elegant rounded top corners.
 * - Active tab renders as an expanded pill containing both icon and label text.
 * - Inactive tabs display only their icons (labels hidden).
 * - Smooth spring transitions for expanding/collapsing pills and fading text.
 * - Preserves the FocusFlow forest green color tokens and contrast.
 */
@Composable
private fun FocusFlowBottomBar(
    destinations: List<TopLevelDestination>,
    currentDestination: NavDestination?,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    FocusFlowBottomBarContent(
        destinations = destinations,
        isRouteSelected = { route ->
            currentDestination?.hierarchy?.any { it.route == route } == true
        },
        onNavigateToDestination = onNavigateToDestination,
        modifier = modifier,
    )
}

@Composable
private fun FocusFlowBottomBarContent(
    destinations: List<TopLevelDestination>,
    isRouteSelected: (String) -> Boolean,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(40.dp),
            color = Color.White.copy(alpha = 0.98f),
            shadowElevation = 10.dp,
            border = BorderStroke(1.2.dp, Color(0xFFE2EBE5).copy(alpha = 0.85f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEach { destination ->
                    val selected = isRouteSelected(destination.route)
                    FocusNavBarItem(
                        destination = destination,
                        selected = selected,
                        onClick = { onNavigateToDestination(destination) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusNavBarItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(destination.labelRes)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    color = if (selected) Color.White.copy(alpha = 0.25f) else FocusForestGreenDeep.copy(alpha = 0.15f),
                ),
                onClick = onClick,
            )
            .semantics {
                role = Role.Tab
                this.selected = selected
                contentDescription = label
            },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = FocusForestGreenDeep,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = destination.selectedIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(9.dp))
                    Text(
                        text = label,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = destination.unselectedIcon,
                    contentDescription = null,
                    tint = FocusNavInactive,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE0E8E3)
@Composable
private fun FocusFlowBottomBarPreview() {
    FocusFlowBottomBarContent(
        destinations = topLevelDestinations,
        isRouteSelected = { it == ROUTE_HOME },
        onNavigateToDestination = {},
    )
}

