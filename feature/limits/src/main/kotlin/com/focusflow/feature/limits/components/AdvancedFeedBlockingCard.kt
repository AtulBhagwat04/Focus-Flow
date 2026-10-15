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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.theme.FocusMutedText
import com.focusflow.core.domain.blocking.model.AdvancedBlockingConfig

@Composable
fun AdvancedFeedBlockingCard(
    config: AdvancedBlockingConfig,
    isAccessibilityEnabled: Boolean,
    onRequestAccessibility: () -> Unit,
    onToggleShorts: (Boolean) -> Unit,
    onToggleReels: (Boolean) -> Unit,
    onToggleFocusOnly: (Boolean) -> Unit,
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
            // Header Row: Icon squircle + title + description
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = FocusForestGreenDeep,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Shorts & Reels Blocking",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FocusDarkText,
                    )
                    Text(
                        text = "Gently exit fast-scrolling feeds without disabling apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusMutedText,
                    )
                }
            }

            // Accessibility Warning Banner (soft neutral warning)
            if (!isAccessibilityEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFF9EE),
                    border = BorderStroke(1.dp, Color(0xFFFFE0B2)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Accessibility needed for feed detection",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF78350F),
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFB45309),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onRequestAccessibility() },
                        ) {
                            Text(
                                text = "Setup",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = FocusCardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Switch 1: YouTube Shorts
            FeedSwitchRow(
                title = "Block YouTube Shorts",
                subtitle = "Redirects out of Shorts back to focus",
                checked = config.isShortsBlockingEnabled,
                onCheckedChange = onToggleShorts,
                enabled = isAccessibilityEnabled,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Switch 2: Instagram Reels
            FeedSwitchRow(
                title = "Block Instagram Reels",
                subtitle = "Redirects out of the Reels player",
                checked = config.isReelsBlockingEnabled,
                onCheckedChange = onToggleReels,
                enabled = isAccessibilityEnabled,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Switch 3: Block Only During Focus Sessions
            FeedSwitchRow(
                title = "Block Only During Focus Sessions",
                subtitle = "Allows feeds during leisure and breaks",
                checked = config.blockDuringFocusOnly,
                onCheckedChange = onToggleFocusOnly,
                enabled = true,
            )
        }
    }
}

@Composable
private fun FeedSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (enabled) FocusDarkText else FocusMutedText,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = FocusMutedText,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = FocusForestGreen,
                uncheckedTrackColor = Color(0xFFE2EBE5),
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}
