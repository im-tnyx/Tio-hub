package com.tnyx.core.theme.tokens.semantic

import androidx.compose.ui.graphics.Color
import com.tnyx.core.theme.tokens.domain.NutritionColors

/**
 * Tnyx Theme Colors Semantic Data Class
 */
data class TnyxColors(
    val primary: Color,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceVariant: Color,
    
    // Monochrome Contextual Accent
    val accent: Color,       
    
    // Content Roles
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val onPrimary: Color,
    
    // Semantic States
    val error: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    
    // Domain Bridge
    val nutrition: NutritionColors,
    val ai: Color,
    
    val isDark: Boolean,
    val isOled: Boolean = false
)
