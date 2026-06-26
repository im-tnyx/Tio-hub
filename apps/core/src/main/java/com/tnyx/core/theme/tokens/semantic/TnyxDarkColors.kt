package com.tnyx.core.theme.tokens.semantic

import androidx.compose.ui.graphics.Color
import com.tnyx.core.theme.tokens.domain.DefaultNutritionColors
import com.tnyx.core.theme.tokens.foundation.TnyxPalette

/**
 * Tnyx Dark Mode Theme Palette
 */
val TnyxDarkColors = TnyxColors(
    primary = TnyxPalette.White,
    background = TnyxPalette.Neutral950,
    surface = TnyxPalette.Neutral900,
    surfaceRaised = TnyxPalette.Neutral800,
    surfaceVariant = TnyxPalette.Neutral800,
    accent = TnyxPalette.White,
    textPrimary = TnyxPalette.White,
    textSecondary = TnyxPalette.Neutral400,
    textMuted = TnyxPalette.Neutral500,
    onPrimary = TnyxPalette.Black,
    error = Color(0xFFEF4444),
    success = Color(0xFF22C55E),
    warning = Color(0xFFF59E0B),
    info = Color(0xFF3B82F6),
    nutrition = DefaultNutritionColors,
    ai = Color(0xFF818CF8),
    isDark = true
)
