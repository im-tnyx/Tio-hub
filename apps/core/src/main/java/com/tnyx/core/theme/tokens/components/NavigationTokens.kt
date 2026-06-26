package com.tnyx.core.theme.tokens.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tnyx Design System - Navigation Component Tokens
 */
data class NavigationTokens(
    // Main Bottom Navigation
    val bottomNavHeight: Dp = 52.dp,
    val bottomNavIconSize: Dp = 24.dp,
    val bottomNavRippleSize: Dp = 32.dp,
    val bottomNavAiIconSize: Dp = 42.dp,
    val bottomNavDividerAlpha: Float = 0.08f,
    
    // Workout Secondary Navigation
    val workoutSecondaryNavHeight: Dp = 44.dp,
    val workoutSecondaryNavIconSize: Dp = 18.dp,
    val workoutSecondaryNavCornerRadius: Dp = 24.dp,
    val workoutSecondaryNavBackgroundAlpha: Float = 0.65f,
    val workoutSecondaryNavBorderAlpha: Float = 0.08f
)
