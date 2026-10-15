package com.focusflow.feature.limits.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Tune
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

@Composable
fun AppLimitItemCard(
    item: AppLimitUiItem,
    onToggleBlockDuringFocus: (Boolean) -> Unit,
    onEditLimitsClicked: () -> Unit,
    onToggleEssential: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, FocusCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: App icon squircle, App details, Safe List badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // App squircle initial
                val initial = item.appName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "A"
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE8F4EE)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FocusForestGreenDeep,
                        fontSize = 18.sp,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FocusDarkText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusMutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (item.isSafeListed) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = FocusForestMintBadge,
                        border = BorderStroke(1.dp, FocusForestMintBorder.copy(alpha = 0.4f)),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = FocusForestGreen,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Safe List",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FocusForestGreenDeep,
                            )
                        }
                    }
                }
            }

            if (!item.isSafeListed) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = FocusCardBorder,
                    thickness = 1.dp,
                )

                // Block During Focus Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Block during Focus",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = FocusDarkText,
                        )
                        Text(
                            text = "Silenced when a session is active",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusMutedText,
                        )
                    }

                    Switch(
                        checked = item.blockDuringFocus,
                        onCheckedChange = onToggleBlockDuringFocus,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FocusForestGreen,
                            uncheckedTrackColor = Color(0xFFE2EBE5),
                            uncheckedThumbColor = Color.White,
                            uncheckedBorderColor = Color.Transparent,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Daily Limits Summary & Configure Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val hasLimits = item.dailyTimeLimitMinutes != null || item.dailyLaunchLimit != null
                    if (hasLimits) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FocusForestMintBg,
                            border = BorderStroke(1.dp, FocusForestMintBorder.copy(alpha = 0.35f)),
                        ) {
                            val limitSummary = buildString {
                                if (item.dailyTimeLimitMinutes != null) {
                                    append("${item.dailyTimeLimitMinutes}m / day")
                                }
                                if (item.dailyLaunchLimit != null) {
                                    if (isNotEmpty()) append(" • ")
                                    append("${item.dailyLaunchLimit} launches")
                                }
                            }
                            Text(
                                text = limitSummary,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = FocusForestGreenDeep,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    } else {
                        Text(
                            text = "No daily cap set",
                            style = MaterialTheme.typography.bodySmall,
                            color = FocusMutedText,
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F6F3),
                        border = BorderStroke(1.dp, FocusForestMintBorder.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onEditLimitsClicked() },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = FocusForestGreenDeep,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Set Limit",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = FocusForestGreenDeep,
                            )
                        }
                    }
                }
            }
        }
    }
}
