package com.focusflow.feature.timer.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreen
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusMutedText

/**
 * Block Apps toggle card with icon badge, title/subtitle, switch toggle,
 * and navigation chevron to open app blocking settings.
 */
@Composable
fun BlockAppsCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onNavigateToLimits: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, if (isEnabled) Color(0xFFCCE3D5) else Color(0xFFE2EBE5)),
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (isEnabled) Color(0xFFDCEFE3) else Color(0xFFEAF1EC)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Block,
                    contentDescription = null,
                    tint = if (isEnabled) FocusForestGreenDeep else Color(0xFF3B5D4B),
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Subtitle (clickable to navigate to limits settings)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = null,
                        indication = ripple(bounded = false, radius = 40.dp),
                        onClick = onNavigateToLimits,
                    ),
            ) {
                Text(
                    text = "Block Apps",
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF14291E),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isEnabled) "Strict blocking enabled" else "Keep distractions away",
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isEnabled) FocusForestGreen else FocusMutedText,
                )
            }

            // Switch Toggle (independent click target)
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = FocusForestGreenDeep,
                    checkedBorderColor = Color.Transparent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD4E0D9),
                    uncheckedBorderColor = Color.Transparent,
                ),
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Chevron to open limits
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = null,
                        indication = ripple(bounded = true, radius = 16.dp),
                        onClick = onNavigateToLimits,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Configure blocked apps",
                    tint = FocusForestGreenDeep,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
