package com.focusflow.feature.timer.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.focusflow.core.designsystem.component.CustomDurationDialog as CoreCustomDurationDialog

/**
 * Custom Duration Dialog for the focus timer screen, reusing the botanical custom time facility
 * from the design system / home screen focus card.
 */
@Composable
fun CustomDurationDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    CoreCustomDurationDialog(
        currentMinutes = currentMinutes,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        modifier = modifier,
    )
}
