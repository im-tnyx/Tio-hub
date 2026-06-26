package com.tnyx.core.theme.tokens.domain

import androidx.compose.ui.graphics.Color
import com.tnyx.core.theme.tokens.foundation.TnyxPalette

/**
 * Tnyx Domain Specific Colors - Nutrition
 */
data class NutritionColors(
    val protein: Color,
    val carbs: Color,
    val fats: Color,
    val calories: Color
)

val DefaultNutritionColors = NutritionColors(
    protein = TnyxPalette.Blue,
    carbs = TnyxPalette.Amber,
    fats = TnyxPalette.Emerald,
    calories = TnyxPalette.Indigo
)
