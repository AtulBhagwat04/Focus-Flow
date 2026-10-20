package com.focusflow.feature.profile.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.focusflow.feature.profile.R

/**
 * Entry points to the rest of the app's controls, gathered from the Profile tab
 * per UI_UX_SPEC.md §3 (Settings, permission health and limits are reached from
 * Profile or Home cards).
 */
@Composable
fun ProfileToolsCard(
    onNavigateToLimits: () -> Unit,
    onNavigateToPermissionHealth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileSectionCard(modifier = modifier) {
        ProfileSectionHeader(
            icon = Icons.Outlined.Settings,
            title = stringResource(R.string.profile_tools_title),
            subtitle = stringResource(R.string.profile_tools_subtitle),
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProfileActionRow(
            icon = Icons.Outlined.Block,
            title = stringResource(R.string.profile_link_blocking_title),
            subtitle = stringResource(R.string.profile_link_blocking_subtitle),
            onClick = onNavigateToLimits,
        )

        ProfileActionRow(
            icon = Icons.Outlined.HealthAndSafety,
            title = stringResource(R.string.profile_link_health_title),
            subtitle = stringResource(R.string.profile_link_health_subtitle),
            onClick = onNavigateToPermissionHealth,
        )
    }
}
