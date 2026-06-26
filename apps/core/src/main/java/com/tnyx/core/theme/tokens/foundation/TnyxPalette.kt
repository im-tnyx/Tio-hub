package com.tnyx.core.theme.tokens.foundation

import androidx.compose.ui.graphics.Color

/**
 * Tnyx Design System Primitives
 * Raw color values for the core monochrome palette and domain exceptions.
 */
object TnyxPalette {
    // Monochrome Core
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
    val PureBlack = Color(0xFF000000) // OLED Optimized

    // Detailed Grayscale
    val Neutral050 = Color(0xFFF8F9FA)
    val Neutral100 = Color(0xFFF5F5F5)
    val Neutral200 = Color(0xFFE8E8E8)
    val Neutral300 = Color(0xFFE0E0E0)
    val Neutral400 = Color(0xFFAFAFAF)
    val Neutral500 = Color(0xFF657786)
    val Neutral600 = Color(0xFF4B5563)
    val Neutral700 = Color(0xFF333333)
    val Neutral800 = Color(0xFF1A1A1A)
    val Neutral900 = Color(0xFF14171A)
    val Neutral950 = Color(0xFF121212)

    // Domain Primitives (Fixed values for visualization)
    val Blue = Color(0xFF3B82F6)
    val Amber = Color(0xFFF59E0B)
    val Emerald = Color(0xFF10B981)
    val Indigo = Color(0xFF6366F1)
    val Rose = Color(0xFFF43F5E)
}
