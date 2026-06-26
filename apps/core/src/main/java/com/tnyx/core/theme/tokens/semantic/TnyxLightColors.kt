package com.tnyx.core.theme.tokens.semantic

import androidx.compose.ui.graphics.Color
import com.tnyx.core.theme.tokens.domain.DefaultNutritionColors
import com.tnyx.core.theme.tokens.foundation.TnyxPalette

/**
 * Tnyx Light Mode Theme Palette
 */
val TnyxLightColors = TnyxColors(
    primary = TnyxPalette.Black,
    background = TnyxPalette.White,
    surface = TnyxPalette.Neutral050,
    surfaceRaised = TnyxPalette.White,
    surfaceVariant = TnyxPalette.Neutral100,
    accent = TnyxPalette.Black,
    textPrimary = TnyxPalette.Black,
    textSecondary = TnyxPalette.Neutral600,
    textMuted = TnyxPalette.Neutral400,
    onPrimary = TnyxPalette.White,
    error = Color(0xFFB91C1C),
    success = Color(0xFF15803D),
    warning = Color(0xFFB45309),
    info = Color(0xFF1D4ED8),
    nutrition = DefaultNutritionColors,
    ai = Color(0xFF6366F1),
    isDark = false
)
