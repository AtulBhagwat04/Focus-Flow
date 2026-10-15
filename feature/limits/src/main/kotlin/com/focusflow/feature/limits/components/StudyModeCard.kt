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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyModeCard(
    config: StudyModeConfig,
    channels: List<StudyChannel>,
    isAccessibilityEnabled: Boolean,
    onToggleStudyMode: (Boolean) -> Unit,
    onToggleFailClosed: (Boolean) -> Unit,
    onToggleChannel: (channelId: String, isAllowlisted: Boolean) -> Unit,
    onAddChannelClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, FocusCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Squircle icon, title, master switch
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
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(FocusForestMintBadge),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = FocusForestGreenDeep,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "YouTube Study Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FocusDarkText,
                        )
                        Text(
                            text = "Permit educational content only",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusMutedText,
                        )
                    }
                }

                Switch(
                    checked = config.isEnabled,
                    onCheckedChange = onToggleStudyMode,
                    enabled = isAccessibilityEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FocusForestGreen,
                        uncheckedTrackColor = Color(0xFFE2EBE5),
                        uncheckedThumbColor = Color.White,
                        uncheckedBorderColor = Color.Transparent,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Allows videos from verified educational channels while silencing recommendations and entertainment.",
                style = MaterialTheme.typography.bodySmall,
                color = FocusMutedText,
            )

            if (config.isEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = FocusCardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Strict Mode (Fail-Closed)
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
                            text = "Block video immediately if channel cannot be verified",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusMutedText,
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = config.failClosed,
                        onCheckedChange = onToggleFailClosed,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FocusForestGreen,
                            uncheckedTrackColor = Color(0xFFE2EBE5),
                            uncheckedThumbColor = Color.White,
                            uncheckedBorderColor = Color.Transparent,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Allowed Channels Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Allowed Channels",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FocusDarkText,
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F6F3),
                        border = BorderStroke(1.dp, FocusForestMintBorder.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAddChannelClicked() },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Channel",
                                tint = FocusForestGreenDeep,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FocusForestGreenDeep,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Channels FlowRow
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    channels.forEach { channel ->
                        FilterChip(
                            selected = channel.isAllowlisted,
                            onClick = {
                                onToggleChannel(channel.channelId, !channel.isAllowlisted)
                            },
                            label = {
                                Text(
                                    text = channel.channelTitle,
                                    fontSize = 12.sp,
                                    fontWeight = if (channel.isAllowlisted) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FocusForestMintBadge,
                                selectedLabelColor = FocusForestGreenDeep,
                                containerColor = Color(0xFFF7FAF8),
                                labelColor = FocusMutedText,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = channel.isAllowlisted,
                                borderColor = FocusCardBorder,
                                selectedBorderColor = FocusForestMintBorder,
                                borderWidth = 1.dp,
                            ),
                        )
                    }
                }
            }
        }
    }
}
