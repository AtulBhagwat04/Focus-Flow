package com.focusflow.feature.stats.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.OutfitFontFamily
import com.focusflow.feature.stats.R

/**
 * Compact period navigation row featuring left/right chevrons and centered date label.
 */
@Composable
fun StatsDateNavigator(
    dateLabel: String,
    canNavigateNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val prevCd = stringResource(R.string.stats_cd_previous_period)
    val nextCd = stringResource(R.string.stats_cd_next_period)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Previous Chevron
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 17.dp),
                    onClick = onPrevious,
                )
                .semantics {
                    role = Role.Button
                    contentDescription = prevCd
                },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = FocusDarkText,
                modifier = Modifier.size(20.dp),
            )
        }

        // Formatted Date Label
        Text(
            text = dateLabel,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.5.sp,
            color = FocusDarkText,
        )

        // Next Chevron
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .then(
                    if (canNavigateNext) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, radius = 17.dp),
                            onClick = onNext,
                        )
                    } else {
                        Modifier
                    }
                )
                .semantics {
                    role = Role.Button
                    contentDescription = nextCd
                },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (canNavigateNext) FocusDarkText else Color(0xFFC0CEC6),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
