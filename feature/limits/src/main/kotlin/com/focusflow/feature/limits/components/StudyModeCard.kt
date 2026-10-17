package com.focusflow.feature.limits.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.theme.FocusForestMintBorder
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.domain.blocking.model.StudyChannel
import com.focusflow.core.domain.blocking.model.StudyModeConfig

private val SwitchTrackUnchecked = Color(0xFFE2EBE5)
private val EmptyStateBg = Color(0xFFF7FAF8)
private val AddButtonBg = Color(0xFFF1F6F3)

/**
 * Redesigned YouTube Study Mode card.
 * Does not show predefined channel names — allows users to add and manage their own study channels.
 */
@Composable
fun StudyModeCard(
    config: StudyModeConfig,
    channels: List<StudyChannel>,
    isAccessibilityEnabled: Boolean,
    onToggleStudyMode: (Boolean) -> Unit,
    onToggleFailClosed: (Boolean) -> Unit,
    onToggleChannel: (channelId: String, isAllowlisted: Boolean) -> Unit,
    onAddChannelClicked: () -> Unit,
    onRemoveChannelClicked: (channelId: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, FocusCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            StudyModeHeader(
                isEnabled = config.isEnabled,
                isAccessibilityEnabled = isAccessibilityEnabled,
                onToggle = onToggleStudyMode,
            )

            if (config.isEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = FocusCardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                StrictModeRow(
                    failClosed = config.failClosed,
                    onToggle = onToggleFailClosed,
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = FocusCardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                ChannelsSection(
                    channels = channels,
                    onAddClicked = onAddChannelClicked,
                    onToggleChannel = onToggleChannel,
                    onRemoveChannel = onRemoveChannelClicked,
                )
            }
        }
    }
}

@Composable
private fun StudyModeHeader(
    isEnabled: Boolean,
    isAccessibilityEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(FocusForestMintBadge),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = FocusForestGreenDeep,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "YouTube Study Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FocusDarkText,
                )
                Text(
                    text = "Permit approved educational creators only",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusMutedText,
                )
            }
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            enabled = isAccessibilityEnabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = FocusForestGreen,
                uncheckedTrackColor = SwitchTrackUnchecked,
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun StrictModeRow(
    failClosed: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Strict Mode (Fail-Closed)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = FocusDarkText,
            )
            Text(
                text = "Block video immediately if creator cannot be verified",
                style = MaterialTheme.typography.bodySmall,
                color = FocusMutedText,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = failClosed,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = FocusForestGreen,
                uncheckedTrackColor = SwitchTrackUnchecked,
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun ChannelsSection(
    channels: List<StudyChannel>,
    onAddClicked: () -> Unit,
    onToggleChannel: (String, Boolean) -> Unit,
    onRemoveChannel: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Allowed Channels",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = FocusDarkText,
            )
            if (channels.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = FocusForestMintBadge,
                    modifier = Modifier.size(20.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = channels.size.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusForestGreenDeep,
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = AddButtonBg,
            border = BorderStroke(1.dp, FocusForestMintBorder.copy(alpha = 0.4f)),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onAddClicked),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = FocusForestGreenDeep,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Channel",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = FocusForestGreenDeep,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    if (channels.isEmpty()) {
        EmptyChannelsView()
    } else {
        ChannelsFlowRow(
            channels = channels,
            onToggleChannel = onToggleChannel,
            onRemoveChannel = onRemoveChannel,
        )
    }
}

@Composable
private fun EmptyChannelsView() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = EmptyStateBg,
        border = BorderStroke(1.dp, FocusCardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No allowed channels yet",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = FocusDarkText,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add channels you want to permit during Study Mode. Non-whitelisted videos will be blocked.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusMutedText,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChannelsFlowRow(
    channels: List<StudyChannel>,
    onToggleChannel: (String, Boolean) -> Unit,
    onRemoveChannel: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        channels.forEach { channel ->
            ChannelChip(
                channel = channel,
                onToggle = { onToggleChannel(channel.channelId, !channel.isAllowlisted) },
                onRemove = { onRemoveChannel(channel.channelId) },
            )
        }
    }
}

@Composable
private fun ChannelChip(
    channel: StudyChannel,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
) {
    val bg = if (channel.isAllowlisted) FocusForestMintBadge else Color(0xFFF1F6F3)
    val textCol = if (channel.isAllowlisted) FocusForestGreenDeep else FocusMutedText

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, FocusForestMintBorder.copy(alpha = 0.4f)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Text(
                text = channel.channelTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textCol,
                modifier = Modifier.clickable(onClick = onToggle),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove ${channel.channelTitle}",
                tint = textCol.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove),
            )
        }
    }
}
