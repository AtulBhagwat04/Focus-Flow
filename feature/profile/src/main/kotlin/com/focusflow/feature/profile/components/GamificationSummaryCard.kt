package com.focusflow.feature.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.designsystem.theme.FocusTertiary
import com.focusflow.core.designsystem.theme.FocusTileSurface
import com.focusflow.core.domain.auth.model.UserProfile
import com.focusflow.feature.profile.R

@Composable
fun GamificationSummaryCard(
    profile: UserProfile?,
    modifier: Modifier = Modifier,
) {
    ProfileSectionCard(modifier = modifier) {
        ProfileSectionHeader(
            icon = Icons.Filled.EmojiEvents,
            title = stringResource(R.string.profile_gamification_title),
            subtitle = stringResource(R.string.profile_gamification_subtitle),
        )

        Spacer(modifier = Modifier.height(16.dp))

        GamificationStatGrid(profile = profile)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = FocusCardBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        GamificationMetaRow(profile = profile)
    }
}

@Composable
private fun GamificationStatGrid(
    profile: UserProfile?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileStatTile(
                icon = Icons.Filled.WorkspacePremium,
                label = stringResource(R.string.profile_stat_level),
                value = (profile?.level ?: 1).toString(),
                modifier = Modifier.weight(1f),
            )
            ProfileStatTile(
                icon = Icons.Filled.Stars,
                label = stringResource(R.string.profile_stat_xp),
                value = (profile?.xp ?: 0L).toString(),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileStatTile(
                icon = Icons.Filled.LocalFireDepartment,
                label = stringResource(R.string.profile_stat_streak),
                value = streakLabel(profile?.currentStreakDays ?: 0),
                modifier = Modifier.weight(1f),
            )
            ProfileStatTile(
                icon = Icons.Filled.Paid,
                label = stringResource(R.string.profile_stat_coins),
                value = (profile?.coins ?: 0L).toString(),
                accent = FocusTertiary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun GamificationMetaRow(
    profile: UserProfile?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfileMetaStat(
            icon = Icons.Outlined.Timer,
            label = stringResource(R.string.profile_meta_total_focus),
            value = formatFocusDuration(profile?.totalFocusMinutes ?: 0L),
            modifier = Modifier.weight(1f),
        )
        ProfileMetaStat(
            icon = Icons.Filled.LocalFireDepartment,
            label = stringResource(R.string.profile_meta_longest_streak),
            value = streakLabel(profile?.longestStreakDays ?: 0),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun streakLabel(days: Int): String = pluralStringResource(
    R.plurals.profile_streak_days,
    days,
    days,
)

@Composable
private fun ProfileStatTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = FocusForestGreen,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = FocusTileSurface,
        border = BorderStroke(1.dp, FocusCardBorder),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = FocusMutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FocusDarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProfileMetaStat(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FocusForestGreenDeep,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = FocusDarkText,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = FocusMutedText,
            )
        }
    }
}

@Composable
private fun formatFocusDuration(totalMinutes: Long): String {
    val hours = (totalMinutes / MINUTES_PER_HOUR).toInt()
    val minutes = (totalMinutes % MINUTES_PER_HOUR).toInt()
    return if (hours > 0) {
        stringResource(R.string.profile_focus_hours_minutes, hours, minutes)
    } else {
        stringResource(R.string.profile_focus_minutes, minutes)
    }
}

private const val MINUTES_PER_HOUR = 60
