package com.focusflow.feature.stats.permission

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.core.designsystem.component.FocusFlowAppBar
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.theme.FocusForestMintBorder
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.FocusScreenBackground
import com.focusflow.core.designsystem.theme.OutfitFontFamily
import com.focusflow.core.domain.permission.AppPermission

@Composable
fun PermissionHealthScreen(
    onOpenPermissionSettings: (AppPermission) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PermissionHealthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    val coreItems = uiState.items.filter { it.isCore }
    val optionalItems = uiState.items.filter { !it.isCore }

    Scaffold(
        topBar = {
            FocusFlowAppBar(
                title = "Permission Health",
                onBack = onBack,
                containerColor = Color.Transparent,
            )
        },
        containerColor = FocusScreenBackground,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ── Essential Permissions ──────────────────────────────────────────
            SectionLabel(text = "Essential")

            coreItems.forEach { item ->
                PermissionRow(
                    item = item,
                    onActionClicked = { onOpenPermissionSettings(item.permission) },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Optional Permissions ───────────────────────────────────────────
            SectionLabel(text = "Optional")

            optionalItems.forEach { item ->
                PermissionRow(
                    item = item,
                    onActionClicked = { onOpenPermissionSettings(item.permission) },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        fontFamily = OutfitFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = FocusMutedText,
        letterSpacing = 0.6.sp,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

@Composable
private fun PermissionRow(
    item: PermissionUiItem,
    onActionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isGranted = item.state.isGranted

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            if (isGranted) Color(0xFFE2EBE5) else Color(0xFFEDD5D5),
        ),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        if (isGranted) Color(0xFFEBF6F0) else Color(0xFFF3F5F4)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = resolvePermissionIcon(item.permission),
                    contentDescription = null,
                    tint = if (isGranted) FocusForestGreenDeep else Color(0xFF6B7E74),
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & short description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusDarkText,
                )
                Text(
                    text = item.description,
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = FocusMutedText,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Status / action
            if (isGranted) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(FocusForestMintBadge),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Granted",
                        tint = FocusForestGreenDeep,
                        modifier = Modifier.size(15.dp),
                    )
                }
            } else {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(
                            indication = ripple(bounded = true),
                            interactionSource = null,
                            onClick = onActionClicked,
                        )
                        .semantics {
                            role = Role.Button
                            contentDescription = "Grant ${item.title}"
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = FocusForestGreenDeep,
                    border = BorderStroke(0.dp, Color.Transparent),
                ) {
                    Text(
                        text = if (item.isCore) "Grant" else "Enable",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

private fun resolvePermissionIcon(permission: AppPermission): ImageVector = when (permission) {
    AppPermission.USAGE_ACCESS -> Icons.Outlined.BarChart
    AppPermission.OVERLAY -> Icons.Outlined.Layers
    AppPermission.NOTIFICATIONS -> Icons.Outlined.Notifications
    AppPermission.BATTERY_OPTIMIZATION -> Icons.Outlined.BatteryChargingFull
    AppPermission.ACCESSIBILITY -> Icons.Outlined.Visibility
}
