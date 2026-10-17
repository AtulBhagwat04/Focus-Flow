package com.focusflow.feature.limits.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.focusflow.feature.limits.AppLimitUiItem

/**
 * Adapter wrapper delegating to [AppLimitListItem].
 * Preserves binary compatibility with any existing call sites.
 */
@Composable
fun AppLimitItemCard(
    item: AppLimitUiItem,
    onToggleBlockDuringFocus: (Boolean) -> Unit,
    onEditLimitsClicked: () -> Unit,
    onToggleEssential: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppLimitListItem(
        item = item,
        onToggleBlock = onToggleBlockDuringFocus,
        onSetLimitClicked = onEditLimitsClicked,
        modifier = modifier,
    )
}
