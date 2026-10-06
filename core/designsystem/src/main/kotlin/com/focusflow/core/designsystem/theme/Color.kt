package com.focusflow.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// ─── Brand palette ──────────────────────────────────────────────────────────
// Design principle: "Calm over loud. Few colors, generous spacing, quiet motion."
// Blocking uses a calm neutral tone — never red (which implies error/danger).

// Primary — deep calm blue: the "focus" brand anchor
val FocusPrimary       = Color(0xFF3D6BCC)
val FocusPrimaryDark   = Color(0xFF6B97F0)

// Secondary — muted teal for progress indicators
val FocusSecondary     = Color(0xFF2E8B7A)
val FocusSecondaryDark = Color(0xFF4DBDA8)

// Tertiary — warm gold reserved for rewards / gamification ONLY
val FocusTertiary      = Color(0xFFE8A838)
val FocusTertiaryDark  = Color(0xFFF5C46A)

// Neutral surfaces
val FocusSurface       = Color(0xFFF5F7FF)
val FocusSurfaceDark   = Color(0xFF0F1524)
val FocusSurface2      = Color(0xFFEBEEFB)
val FocusSurface2Dark  = Color(0xFF161D30)

// On-colors
val FocusOnSurface     = Color(0xFF1B1F2E)
val FocusOnSurfaceDark = Color(0xFFE3E7F8)

// Block neutral — calm, not alarming (never use error red for "blocked" state)
val BlockNeutral       = Color(0xFF6B7393)
val BlockNeutralDark   = Color(0xFF9AA3C2)

// Error — reserved for real system errors only
val FocusError         = Color(0xFFBA1A1A)
val FocusErrorDark     = Color(0xFFFFB4AB)
