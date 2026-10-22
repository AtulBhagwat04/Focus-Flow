package com.focusflow.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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

private val HeroBlobColor = Color(0xFFF3ECE0)
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FallbackScreenBg),
        ) {
            Image(
                painter = painterResource(id = com.focusflow.core.designsystem.R.drawable.bg_home_screen),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProfileTopNavigation(onBack = onBack)

                Spacer(modifier = Modifier.height(8.dp))

                val name = uiState.account?.displayName?.takeIf { it.isNotBlank() } ?: "Atul Bhagwat"
                val level = uiState.profile?.level ?: 3
                val xp = uiState.profile?.xp ?: 750L

                ProfileHeroHeader(
                    displayName = name,
                    initials = extractInitials(name),
                    level = level,
                    progressFraction = ((xp % 1000L).toFloat() / 1000f).coerceIn(0.15f, 1f),
                    onEditProfile = { showEditProfileDialog = true },
                )

                Spacer(modifier = Modifier.height(18.dp))

                ProfileStatsCard(
                    totalFocusMinutes = uiState.weeklyFocusMinutes,
                    sessionsCount = uiState.completedSessionsCount,
                    streakDays = uiState.profile?.currentStreakDays?.takeIf { it > 0 } ?: 5,
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = uiState.account?.displayName ?: "Atul Bhagwat",
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
private fun ProfileTopNavigation(
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onBack() },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ForestGreenDeep,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
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
    Box(
        modifier = modifier
            .size(280.dp)
            .clip(CircleShape)
            .background(HeroBlobColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                ),
                color = DarkCharcoalText,
            )

            Spacer(modifier = Modifier.height(14.dp))

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
                    fontSize = 24.sp,
                ),
                color = DarkCharcoalText,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(MintBadgeBg)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${getLevelTitle(level)} · Level $level",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    ),
                    color = ForestGreenDeep,
                )
            }
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
        modifier = modifier.size(96.dp),
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(AvatarBadgeBg)
                .border(2.dp, Color.White, CircleShape)
                .clickable(onClick = onEditProfile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit Profile",
                tint = ForestGreenDeep,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun ProfileStatsCard(
    totalFocusMinutes: Long,
    sessionsCount: Int,
    streakDays: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatColumn(
                value = formatFocusDuration(totalFocusMinutes),
                label = "This week",
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .height(36.dp)
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
                    .height(36.dp)
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
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
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
            .padding(horizontal = 18.dp, vertical = 14.dp),
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
    Button(
        onClick = onSignOut,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SignOutBg,
            contentColor = SignOutText,
        ),
        border = BorderStroke(1.dp, SignOutBorder),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                tint = SignOutText,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sign out",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 16.sp,
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
        title = { Text("Edit Display Name") },
        text = {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Display Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nameInput.trim()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
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
        title = { Text("Weekly Focus Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                goals.forEach { goal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectGoal(goal) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${goal} hours / week",
                            fontWeight = if (goal == currentGoal) FontWeight.Bold else FontWeight.Normal,
                            color = if (goal == currentGoal) ForestGreenDeep else DarkCharcoalText,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
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
        title = { Text("Help & Account Settings") },
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
                Text("Close")
            }
        },
    )
}

@Composable
private fun DialogActionRow(
    text: String,
    onClick: () -> Unit,
    textColor: Color = DarkCharcoalText,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Medium)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = ChevronTint,
            modifier = Modifier.size(18.dp),
        )
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
    val effectiveMinutes = if (minutes > 0) minutes else 750L
    val hours = effectiveMinutes / 60L
    val mins = effectiveMinutes % 60L
    return if (hours > 0 && mins > 0) {
        "${hours}h ${mins}m"
    } else if (hours > 0) {
        "${hours}h"
    } else {
        "${mins}m"
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
