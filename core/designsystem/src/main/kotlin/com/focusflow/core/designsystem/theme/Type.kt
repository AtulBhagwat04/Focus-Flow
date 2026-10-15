package com.focusflow.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.focusflow.core.designsystem.R

// ─── Google Fonts provider ────────────────────────────────────────────────────
// Fetches Outfit at runtime from the Google Fonts service.
// Falls back to the system font if the download fails (e.g. offline).
private val googleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

// ─── Font families ────────────────────────────────────────────────────────────
// Outfit — primary UI font (geometric sans-serif; consistent across all devices)
// Spec ref: UI_UX_SPEC.md §2 – typography
val OutfitFontFamily = FontFamily(
    Font(
        googleFont     = GoogleFont("Outfit"),
        fontProvider   = googleFontsProvider,
        weight         = FontWeight.Normal,
    ),
    Font(
        googleFont     = GoogleFont("Outfit"),
        fontProvider   = googleFontsProvider,
        weight         = FontWeight.Medium,
    ),
    Font(
        googleFont     = GoogleFont("Outfit"),
        fontProvider   = googleFontsProvider,
        weight         = FontWeight.SemiBold,
    ),
    Font(
        googleFont     = GoogleFont("Outfit"),
        fontProvider   = googleFontsProvider,
        weight         = FontWeight.Bold,
    ),
)

// DM Mono — timer digits only (monospace/tabular so digits don't jitter)
// See UI_UX_SPEC.md §2: "The timer uses tabular figures so digits do not jitter."
val TimerFontFamily = FontFamily.Monospace

// ─── Typography scale ─────────────────────────────────────────────────────────
val FocusFlowTypography = Typography(
    // Display — timer digits (large, tabular)
    displayLarge = TextStyle(
        fontFamily = TimerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 57.sp,
        lineHeight = 64.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = TimerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 45.sp,
        lineHeight = 52.sp,
    ),
    // Headline — screen titles
    headlineLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 24.sp,
        lineHeight = 32.sp,
    ),
    // Title — card headers, section labels
    titleLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // Body — main content
    bodyLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    // Label — buttons, chips, tabs
    labelLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
