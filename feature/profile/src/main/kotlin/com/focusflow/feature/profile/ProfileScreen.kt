package com.focusflow.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.theme.FocusFlowTheme
import com.focusflow.feature.profile.components.DataExportDialog
import com.focusflow.feature.profile.components.DeleteAccountConfirmDialog
import java.util.Locale

private val DarkCharcoalText = Color(0xFF1B2B23)
private val ForestGreenDeep = Color(0xFF1E4D38)
private val ForestGreenMedium = Color(0xFF276A4E)
private val MintBadgeBg = Color(0xFFDBEBDF)
private val MintIconBg = Color(0xFFE2F0E7)
private val PinkIconBg = Color(0xFFFDECEB)
private val PinkIconTint = Color(0xFFD34848)
private val BlueIconBg = Color(0xFFEBF0FA)
private val BlueIconTint = Color(0xFF4A72B2)
private val SignOutBg = Color(0xFFFDF0EF)
private val SignOutBorder = Color(0xFFF7D8D6)
private val SignOutText = Color(0xFFBA3B3B)
private val MutedLabelColor = Color(0xFF6B7E74)
private val ChevronTint = Color(0xFF8DA095)
private val DividerColor = Color(0xFFF3F5F3)
private val AvatarBadgeBg = Color(0xFFD8E8DD)
private val AvatarRingTrackColor = Color(0xFFD4E3DA)
private val FallbackScreenBg = Color(0xFFE8EFE9)
private val StatDividerColor = Color(0xFFEAEAEA)

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onNavigateToLimits: () -> Unit = {},
    onNavigateToPermissionHealth: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onClearMessage()
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        actions = ProfileActions(
            onSync = viewModel::onSyncClicked,
            onExport = viewModel::onExportDataClicked,
            onSignOut = viewModel::onSignOutClicked,
            onDeleteRequested = { showDeleteDialog = true },
            onNavigateToLimits = onNavigateToLimits,
            onNavigateToPermissionHealth = onNavigateToPermissionHealth,
            onToggleReminders = viewModel::onToggleReminders,
            onSelectAppearance = viewModel::onSelectAppearance,
            onUpdateWeeklyGoal = viewModel::onUpdateWeeklyGoal,
            onUpdateDisplayName = viewModel::onUpdateDisplayName,
        ),
        modifier = modifier,
    )

    if (showDeleteDialog) {
        DeleteAccountConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.onDeleteAccountClicked()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    uiState.exportedDataJson?.let { json ->
        DataExportDialog(
            jsonContent = json,
            onDismiss = viewModel::onDismissExport,
        )
    }
}

/** Stateless content composable for the redesigned Profile screen. */
@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    actions: ProfileActions,
    onBack: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showWeeklyGoalDialog by remember { mutableStateOf(false) }
    var showHelpFeedbackDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FallbackScreenBg),
    ) {
        Image(
            painter = painterResource(id = com.focusflow.core.designsystem.R.drawable.bg_home_screen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Soft Ambient Frost White Overlay for Unified Aesthetics & Content Legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = 0.20f),
                        0.22f to Color(0xFFF7FAF8).copy(alpha = 0.65f),
                        0.55f to Color(0xFFF7FAF8).copy(alpha = 0.88f),
                        1.0f to Color(0xFFF7FAF8).copy(alpha = 0.95f),
                    )
                )
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                ProfileTopAppBar(onBack = onBack)
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                val name = uiState.account?.displayName?.takeIf { it.isNotBlank() } ?: "Atul Bhagwat"
                val level = uiState.level
                val xp = uiState.xp

                ProfileHeroHeader(
                    displayName = name,
                    initials = extractInitials(name),
                    level = level,
                    progressFraction = ((xp % 1000L).toFloat() / 1000f).coerceIn(0f, 1f),
                    onEditProfile = { showEditProfileDialog = true },
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileStatsCard(
                    weeklyFocusFormatted = uiState.weeklyFocusFormatted,
                    sessionsCount = uiState.completedSessionsCount,
                    streakDays = uiState.currentStreakDays,
                )

                Spacer(modifier = Modifier.height(14.dp))

                ProfileSettingsCard(
                    weeklyGoalHours = uiState.weeklyGoalHours,
                    blockedAppsCount = uiState.blockedAppsCount,
                    remindersEnabled = uiState.remindersEnabled,
                    onWeeklyGoalClick = { showWeeklyGoalDialog = true },
                    onAppBlockingClick = actions.onNavigateToLimits,
                    onRemindersToggle = actions.onToggleReminders,
                    onHelpFeedbackClick = { showHelpFeedbackDialog = true },
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileSignOutButton(onSignOut = actions.onSignOut)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = uiState.account?.displayName ?: stringResource(R.string.profile_account_default_name),
            onConfirm = { newName ->
                showEditProfileDialog = false
                actions.onUpdateDisplayName(newName)
            },
            onDismiss = { showEditProfileDialog = false },
        )
    }

    if (showWeeklyGoalDialog) {
        WeeklyGoalDialog(
            currentGoal = uiState.weeklyGoalHours,
            onSelectGoal = { goal ->
                showWeeklyGoalDialog = false
                actions.onUpdateWeeklyGoal(goal)
            },
            onDismiss = { showWeeklyGoalDialog = false },
        )
    }

    if (showHelpFeedbackDialog) {
        HelpFeedbackDialog(
            onSync = actions.onSync,
            onExport = actions.onExport,
            onNavigateToHealth = actions.onNavigateToPermissionHealth,
            onDeleteAccount = actions.onDeleteRequested,
            onDismiss = { showHelpFeedbackDialog = false },
        )
    }
}

@Composable
private fun ProfileTopAppBar(
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ForestGreenDeep,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Text(
            text = "Profile",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            ),
            color = DarkCharcoalText,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun ProfileHeroHeader(
    displayName: String,
    initials: String,
    level: Int,
    progressFraction: Float,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileAvatarRing(
            initials = initials,
            progressFraction = progressFraction,
            onEditProfile = onEditProfile,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            ),
            color = DarkCharcoalText,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MintBadgeBg,
            border = BorderStroke(1.dp, Color(0xFFC7DBCB)),
        ) {
            Text(
                text = "${getLevelTitle(level)} · Level $level",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                ),
                color = ForestGreenDeep,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun ProfileAvatarRing(
    initials: String,
    progressFraction: Float,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(96.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onEditProfile,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = 3.5.dp.toPx()
            val diameter = size.minDimension - strokeWidthPx
            val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = AvatarRingTrackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx),
            )

            drawArc(
                color = ForestGreenDeep,
                startAngle = -90f,
                sweepAngle = 270f * progressFraction.coerceIn(0.1f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            )
        }

        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(ForestGreenDeep),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                ),
                color = Color.White,
            )
        }

        Surface(
            shape = CircleShape,
            color = AvatarBadgeBg,
            border = BorderStroke(2.dp, Color.White),
            shadowElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .size(30.dp)
                .clickable(onClick = onEditProfile),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit Profile",
                    tint = ForestGreenDeep,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}

@Composable
private fun ProfileStatsCard(
    weeklyFocusFormatted: String,
    sessionsCount: Int,
    streakDays: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, Color(0xFFE2EBE5).copy(alpha = 0.85f)),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatColumn(
                value = weeklyFocusFormatted,
                label = "This week",
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .width(1.dp)
                    .background(StatDividerColor),
            )
            StatColumn(
                value = sessionsCount.toString(),
                label = "Sessions",
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .width(1.dp)
                    .background(StatDividerColor),
            )
            StatColumn(
                value = "$streakDays days",
                label = "Streak",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
            ),
            color = DarkCharcoalText,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.5.sp,
            ),
            color = MutedLabelColor,
        )
    }
}

@Composable
private fun ProfileSettingsCard(
    weeklyGoalHours: Int,
    blockedAppsCount: Int,
    remindersEnabled: Boolean,
    onWeeklyGoalClick: () -> Unit,
    onAppBlockingClick: () -> Unit,
    onRemindersToggle: (Boolean) -> Unit,
    onHelpFeedbackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, Color(0xFFE2EBE5).copy(alpha = 0.85f)),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SettingRow(
                icon = Icons.Outlined.TrackChanges,
                iconBg = MintIconBg,
                iconTint = ForestGreenMedium,
                title = "Weekly focus goal",
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${weeklyGoalHours}h",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = MutedLabelColor,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = ChevronTint,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                onClick = onWeeklyGoalClick,
            )

            HorizontalDivider(color = DividerColor, thickness = 1.dp)

            SettingRow(
                icon = Icons.Outlined.Block,
                iconBg = PinkIconBg,
                iconTint = PinkIconTint,
                title = "App blocking",
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$blockedAppsCount apps",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = MutedLabelColor,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = ChevronTint,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                onClick = onAppBlockingClick,
            )

            HorizontalDivider(color = DividerColor, thickness = 1.dp)

            SettingRow(
                icon = Icons.Outlined.Notifications,
                iconBg = BlueIconBg,
                iconTint = BlueIconTint,
                title = "Reminders",
                trailing = {
                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = onRemindersToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF22573F),
                            uncheckedThumbColor = Color(0xFF8E9E95),
                            uncheckedTrackColor = Color(0xFFE2E7E4),
                            uncheckedBorderColor = Color.Transparent,
                        ),
                    )
                },
                onClick = { onRemindersToggle(!remindersEnabled) },
            )

            HorizontalDivider(color = DividerColor, thickness = 1.dp)

            SettingRow(
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                iconBg = MintIconBg,
                iconTint = ForestGreenMedium,
                title = "Help & feedback",
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = ChevronTint,
                        modifier = Modifier.size(18.dp),
                    )
                },
                onClick = onHelpFeedbackClick,
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    trailing: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            ),
            color = DarkCharcoalText,
            modifier = Modifier.weight(1f),
        )

        trailing()
    }
}

@Composable
private fun ProfileSignOutButton(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onSignOut,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        color = SignOutBg,
        border = BorderStroke(1.dp, SignOutBorder),
        shadowElevation = 0.5.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                tint = SignOutText,
                modifier = Modifier.size(19.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign out",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = SignOutText,
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var nameInput by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Display Name",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                ),
                color = DarkCharcoalText,
            )
        },
        text = {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Display Name") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nameInput.trim()) },
                enabled = nameInput.isNotBlank(),
            ) {
                Text(
                    text = "Save",
                    color = if (nameInput.isNotBlank()) ForestGreenDeep else MutedLabelColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = MutedLabelColor,
                )
            }
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White,
    )
}

@Composable
private fun WeeklyGoalDialog(
    currentGoal: Int,
    onSelectGoal: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val goals = listOf(10, 12, 14, 16, 20, 24, 30)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Weekly Focus Goal",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                ),
                color = DarkCharcoalText,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                goals.forEach { goal ->
                    val isSelected = goal == currentGoal
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MintBadgeBg else Color.Transparent,
                        border = if (isSelected) BorderStroke(1.dp, Color(0xFFC7DBCB)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectGoal(goal) },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${goal} hours / week",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.5.sp,
                                ),
                                color = if (isSelected) ForestGreenDeep else DarkCharcoalText,
                            )
                            if (isSelected) {
                                Text(
                                    text = "Current",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                    ),
                                    color = ForestGreenMedium,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = ForestGreenDeep,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White,
    )
}

@Composable
private fun HelpFeedbackDialog(
    onSync: () -> Unit,
    onExport: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onDeleteAccount: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Help & Account Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                ),
                color = DarkCharcoalText,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DialogActionRow(text = "Sync Cloud Data", onClick = { onSync(); onDismiss() })
                DialogActionRow(text = "Export Data (JSON)", onClick = { onExport(); onDismiss() })
                DialogActionRow(
                    text = "Device Health & Permissions",
                    onClick = { onNavigateToHealth(); onDismiss() },
                )
                DialogActionRow(
                    text = "Delete Account & Data",
                    textColor = SignOutText,
                    onClick = { onDeleteAccount(); onDismiss() },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = ForestGreenDeep,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White,
    )
}

@Composable
private fun DialogActionRow(
    text: String,
    onClick: () -> Unit,
    textColor: Color = DarkCharcoalText,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                ),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ChevronTint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private fun getLevelTitle(level: Int): String = when (level) {
    1 -> "Seed"
    2 -> "Sprout"
    3 -> "Sapling"
    4 -> "Tree"
    5 -> "Forest"
    else -> "Elder Tree"
}

private fun extractInitials(name: String): String {
    val words = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "AB"
        words.size == 1 -> words.first().take(2).uppercase(Locale.getDefault())
        else -> "${words[0].first().uppercaseChar()}${words[1].first().uppercaseChar()}"
    }
}

private fun formatFocusDuration(minutes: Long): String {
    val hours = minutes / 60L
    val mins = minutes % 60L
    return when {
        hours > 0L && mins > 0L -> "${hours}h ${mins}m"
        hours > 0L -> "${hours}h"
        mins > 0L -> "${mins}m"
        else -> "0m"
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    FocusFlowTheme {
        ProfileScreenContent(
            uiState = ProfileUiState(),
            actions = ProfileActions(
                onSync = {},
                onExport = {},
                onSignOut = {},
                onDeleteRequested = {},
                onNavigateToLimits = {},
                onNavigateToPermissionHealth = {},
            ),
            onBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
