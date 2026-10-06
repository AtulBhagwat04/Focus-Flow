package com.focusflow.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ─── Shape scale ─────────────────────────────────────────────────────────────
// Consistent corner radii by component size — per UI_UX_SPEC.md §2
val FocusFlowShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // chips, small badges
    small      = RoundedCornerShape(8.dp),   // text fields, small cards
    medium     = RoundedCornerShape(12.dp),  // cards, dialogs
    large      = RoundedCornerShape(16.dp),  // bottom sheets, large cards
    extraLarge = RoundedCornerShape(28.dp),  // FAB, blocker overlay
)
