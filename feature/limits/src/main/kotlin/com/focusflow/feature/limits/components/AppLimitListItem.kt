package com.focusflow.feature.limits.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusForestMintBorder
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.feature.limits.AppLimitUiItem

private val SwitchTrackUnchecked = Color(0xFFDCE6DF)
private val SwitchDisabledTrack = Color(0xFFEFEFEF)
private val SwitchDisabledThumb = Color(0xFFCCCCCC)
private val LimitButtonBgDefault = Color(0xFFEBF2ED)

@Composable
fun AppLimitListItem(
    item: AppLimitUiItem,
    onToggleBlock: (Boolean) -> Unit,
    onSetLimitClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2EBE5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InstalledAppIcon(
                packageName = item.packageName,
                appName = item.appName,
                size = 46.dp,
            )

            Spacer(modifier = Modifier.width(14.dp))

            AppInfoColumn(
                item = item,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(8.dp))

            LimitOptionButton(
                item = item,
                onClick = onSetLimitClicked,
            )

            Spacer(modifier = Modifier.width(8.dp))

            BlockToggleSwitch(
                item = item,
                onToggle = onToggleBlock,
            )
        }
    }
}

@Composable
private fun AppInfoColumn(
    item: AppLimitUiItem,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = item.appName,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 16.5.sp,
            color = Color(0xFF14291E),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(2.dp))

        AppStatusSubtitle(item = item)
    }
}

@Composable
private fun AppStatusSubtitle(item: AppLimitUiItem) {
    when {
        item.isSafeListed -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = FocusForestGreen,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Safe listed",
                    fontSize = 12.5.sp,
                    color = FocusForestGreenDeep,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        item.dailyTimeLimitMinutes != null -> {
            Text(
                text = "${item.dailyTimeLimitMinutes}m daily cap",
                fontSize = 12.5.sp,
                color = FocusForestGreen,
                fontWeight = FontWeight.Medium,
            )
        }
        item.blockDuringFocus -> {
            Text(
                text = "Blocked in focus",
                fontSize = 12.5.sp,
                color = FocusForestGreen,
                fontWeight = FontWeight.Normal,
            )
        }
        else -> {
            Text(
                text = "Allowed in focus",
                fontSize = 12.5.sp,
                color = FocusMutedText,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun LimitOptionButton(
    item: AppLimitUiItem,
    onClick: () -> Unit,
) {
    val hasLimit = item.dailyTimeLimitMinutes != null || item.dailyLaunchLimit != null
    val bgColor = if (hasLimit) FocusForestMintBadge else LimitButtonBgDefault
    val borderColor = if (hasLimit) FocusForestMintBorder.copy(alpha = 0.5f) else Color.Transparent
    val iconTint = if (hasLimit) FocusForestGreenDeep else Color(0xFF2E533E)

    Surface(
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = if (hasLimit) {
                    "Daily limit active: ${item.dailyTimeLimitMinutes} minutes for ${item.appName}"
                } else {
                    "Set time limit for ${item.appName}"
                }
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun BlockToggleSwitch(
    item: AppLimitUiItem,
    onToggle: (Boolean) -> Unit,
) {
    val checked = if (item.isSafeListed) false else item.blockDuringFocus
    val description = when {
        item.isSafeListed -> "${item.appName} is protected by Safe List and cannot be blocked"
        item.blockDuringFocus -> "Block ${item.appName} during focus is on"
        else -> "Block ${item.appName} during focus is off"
    }

    Switch(
        checked = checked,
        onCheckedChange = { onToggle(!checked) },
        enabled = !item.isSafeListed,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = FocusForestGreenDeep,
            uncheckedTrackColor = SwitchTrackUnchecked,
            uncheckedThumbColor = Color.White,
            checkedBorderColor = Color.Transparent,
            uncheckedBorderColor = Color.Transparent,
            disabledUncheckedTrackColor = SwitchDisabledTrack,
            disabledUncheckedThumbColor = SwitchDisabledThumb,
        ),
        modifier = Modifier.semantics {
            contentDescription = description
        },
    )
}
