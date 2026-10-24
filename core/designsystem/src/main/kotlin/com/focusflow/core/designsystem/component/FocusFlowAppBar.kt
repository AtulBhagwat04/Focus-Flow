package com.focusflow.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.theme.FocusCardBorder
import com.focusflow.core.designsystem.theme.FocusDarkText
import com.focusflow.core.designsystem.theme.FocusForestGreenDeep
import com.focusflow.core.designsystem.theme.FocusForestMintBadge
import com.focusflow.core.designsystem.theme.FocusForestMintBg
import com.focusflow.core.designsystem.theme.FocusForestMintBorder
import com.focusflow.core.designsystem.theme.FocusMutedText

/**
 * Standard unified FocusFlow Top App Bar.
 *
 * Ensures 100% visual consistency and fixed positioning across the entire application:
 * - Automatically applies [statusBarsPadding] so the app bar always sits in the exact same
 *   pixel-perfect spot on every screen without double-padding or jumping.
 * - Standard 40dp circular back button with gentle mint background and ripple.
 * - Bold title with optional subtitle with consistent typography hierarchy.
 * - Slot for action buttons (e.g. [FocusFlowActionButton], [FocusFlowChipBadge], or custom).
 * - Optional bottom divider for subtle separation.
 */
@Composable
fun FocusFlowAppBar(
    modifier: Modifier = Modifier,
    title: String = "",
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    titleContent: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    containerColor: Color = Color.Transparent,
    contentColor: Color = FocusDarkText,
    showDivider: Boolean = false,
    dividerColor: Color = FocusCardBorder,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(
                        start = if (onBack != null || navigationIcon != null) 12.dp else 20.dp,
                        end = 20.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Left slot: navigation icon / back button + title
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (navigationIcon != null) {
                        navigationIcon()
                        Spacer(modifier = Modifier.width(8.dp))
                    } else if (onBack != null) {
                        FocusFlowBackButton(onClick = onBack)
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (titleContent != null) {
                        titleContent()
                    } else if (title.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.semantics { heading() },
                        ) {
                            Text(
                                text = title,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                letterSpacing = (-0.3).sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (!subtitle.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subtitle,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = FocusMutedText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                // Right slot: actions
                if (actions != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        actions()
                    }
                }
            }

            if (showDivider) {
                HorizontalDivider(thickness = 1.dp, color = dividerColor)
            }
        }
    }
}

/**
 * Signature FocusFlow back button (clean, frameless design).
 */
@Composable
fun FocusFlowBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Back",
    containerColor: Color = Color.Transparent,
    iconTint: Color = FocusForestGreenDeep,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * Signature FocusFlow circular action button (e.g. sounds, menu, incognito, sync).
 */
@Composable
fun FocusFlowActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    containerColor: Color = FocusForestMintBg,
    iconTint: Color = FocusForestGreenDeep,
    borderColor: Color = FocusCardBorder,
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(
                indication = ripple(bounded = true, radius = 20.dp),
                interactionSource = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Button
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Signature FocusFlow chip / badge for app bar actions (e.g. "Today", "Host").
 */
@Composable
fun FocusFlowChipBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    containerColor: Color = FocusForestMintBadge,
    textColor: Color = FocusForestGreenDeep,
    borderColor: Color = FocusForestMintBorder.copy(alpha = 0.5f),
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(CircleShape)
                        .clickable(
                            indication = ripple(bounded = true),
                            interactionSource = null,
                            onClick = onClick,
                        )
                } else Modifier
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
        }
    }
}
