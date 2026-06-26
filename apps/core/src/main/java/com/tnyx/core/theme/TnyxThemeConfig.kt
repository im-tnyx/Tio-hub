package com.tnyx.core.theme

/**
 * Tnyx Theme Configuration
 * Controls advanced theme behaviors.
 */
data class TnyxThemeConfig(
    val mode: TnyxThemeMode = TnyxThemeMode.System,
    val useDynamicColor: Boolean = false,
    val highContrast: Boolean = false,
    val reducedMotion: Boolean = false
)
