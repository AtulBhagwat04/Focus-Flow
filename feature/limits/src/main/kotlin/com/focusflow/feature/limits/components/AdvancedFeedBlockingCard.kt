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

import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.text.font.FontFamily

private val SwitchTrackUnchecked = Color(0xFFDCE6DF)
private val AlertBannerBg = Color(0xFFFFF9EE)
private val AlertBannerBorder = Color(0xFFFFE0B2)
private val AlertBannerText = Color(0xFF78350F)
private val AlertButtonBg = Color(0xFFB45309)

/**
 * Card for configuring Shorts & Reels distraction feed blocking.
 */
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
        border = BorderStroke(1.dp, Color(0xFFE2EBE5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            FeedBlockingHeader()

            if (!isAccessibilityEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                AccessibilitySetupBanner(onClick = onRequestAccessibility)
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFEEF3EF), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            FeedSwitchRow(
                title = "Block YouTube Shorts",
                subtitle = "Redirects out of Shorts back to focus",
                checked = config.isShortsBlockingEnabled,
                onCheckedChange = onToggleShorts,
                enabled = isAccessibilityEnabled,
            )

            Spacer(modifier = Modifier.height(14.dp))

            FeedSwitchRow(
                title = "Block Instagram Reels",
                subtitle = "Redirects out of the Reels player",
                checked = config.isReelsBlockingEnabled,
                onCheckedChange = onToggleReels,
                enabled = isAccessibilityEnabled,
            )

            Spacer(modifier = Modifier.height(14.dp))

            FeedSwitchRow(
                title = "Only During Focus Sessions",
                subtitle = "Allows feeds during leisure and breaks",
                checked = config.blockDuringFocusOnly,
                onCheckedChange = onToggleFocusOnly,
                enabled = true,
            )
        }
    }
}

@Composable
private fun FeedBlockingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFDCEFE3)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = FocusForestGreenDeep,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Shorts & Reels Blocking",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 17.5.sp,
                color = Color(0xFF14291E),
            )
            Text(
                text = "Gently exit fast-scrolling feeds without disabling apps",
                fontSize = 12.5.sp,
                color = FocusMutedText,
            )
        }
    }
}

@Composable
private fun AccessibilitySetupBanner(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AlertBannerBg,
        border = BorderStroke(1.dp, AlertBannerBorder),
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
                    tint = AlertButtonBg,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Accessibility needed for feed detection",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AlertBannerText,
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AlertButtonBg,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onClick),
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
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color(0xFF14291E) else FocusMutedText,
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
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
                checkedTrackColor = FocusForestGreenDeep,
                uncheckedTrackColor = SwitchTrackUnchecked,
                uncheckedThumbColor = Color.White,
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}
