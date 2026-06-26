package com.tnyx.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.tnyx.core.theme.locals.*
import com.tnyx.core.theme.tokens.components.*
import com.tnyx.core.theme.tokens.effects.*
import com.tnyx.core.theme.tokens.foundation.*
import com.tnyx.core.theme.tokens.semantic.*
import com.tnyx.core.theme.tokens.typography.TnyxTypography

@Composable
fun TnyxThemeProvider(
    config: TnyxThemeConfig = TnyxThemeConfig(),
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val context = LocalContext.current

    val basePalette = when (config.mode) {
        TnyxThemeMode.Light -> TnyxLightColors
        TnyxThemeMode.Dark -> TnyxDarkColors
        TnyxThemeMode.Oled -> TnyxOledColors
        TnyxThemeMode.System -> if (isSystemDark) TnyxDarkColors else TnyxLightColors
    }
    val palette = if (config.highContrast) basePalette.highContrast() else basePalette
    val motion = if (config.reducedMotion) ReducedTnyxMotion else DefaultTnyxMotion
    val useDynamicColor = config.useDynamicColor && config.mode != TnyxThemeMode.Oled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val materialColorScheme = when {
        useDynamicColor && palette.isDark -> dynamicDarkColorScheme(context)
        useDynamicColor -> dynamicLightColorScheme(context)
        else -> palette.toMaterialColorScheme()
    }

    CompositionLocalProvider(
        LocalTnyxColors provides palette,
        LocalTnyxDimens provides TnyxDimens,
        LocalTnyxInsets provides DefaultTnyxInsets,
        LocalTnyxElevation provides TnyxElevation,
        LocalTnyxTypography provides TnyxTypography,
        LocalTnyxMotion provides motion,
        LocalTnyxShapes provides TnyxShapes,
        LocalTnyxGradients provides TnyxGradients,
        LocalTnyxShadows provides TnyxShadows,
        LocalTnyxComponentTokens provides TnyxComponentTokens(
            button = DefaultButtonTokens.copy(
                containerColor = palette.primary,
                contentColor = palette.onPrimary,
                disabledContainerColor = palette.surfaceVariant,
                disabledContentColor = palette.textMuted
            ),
            input = DefaultInputTokens.copy(
                containerColor = palette.surface,
                textColor = palette.textPrimary,
                placeholderColor = palette.textMuted,
                focusedIndicatorColor = palette.primary,
                unfocusedIndicatorColor = palette.surfaceVariant,
                errorIndicatorColor = palette.error
            ),
            card = DefaultCardTokens.copy(
                containerColor = palette.surface,
                borderColor = palette.textPrimary.copy(alpha = 0.1f)
            ),
            sheet = DefaultSheetTokens.copy(
                containerColor = palette.surfaceRaised,
                contentColor = palette.textPrimary,
                scrimColor = palette.background.copy(alpha = 0.72f),
                dividerColor = palette.textPrimary.copy(alpha = 0.08f)
            ),
            navigation = NavigationTokens(),
            calendar = CalendarTokens()
        )
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = TnyxTypography,
            shapes = TnyxShapes.Material,
            content = content
        )
    }
}

private fun TnyxColors.highContrast(): TnyxColors = copy(
    surfaceVariant = if (isDark) TnyxPalette.Neutral700 else TnyxPalette.Neutral200,
    textSecondary = if (isDark) TnyxPalette.Neutral200 else TnyxPalette.Neutral800,
    textMuted = if (isDark) TnyxPalette.Neutral300 else TnyxPalette.Neutral700
)

private fun TnyxColors.toMaterialColorScheme(): ColorScheme {
    val onStateColor = if (isDark) TnyxPalette.Black else TnyxPalette.White
    val inverseSurface = if (isDark) TnyxPalette.Neutral100 else TnyxPalette.Neutral900
    val inverseOnSurface = if (isDark) TnyxPalette.Neutral900 else TnyxPalette.Neutral100

    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = surfaceRaised,
            onPrimaryContainer = textPrimary,
            secondary = accent,
            onSecondary = onPrimary,
            secondaryContainer = surfaceVariant,
            onSecondaryContainer = textPrimary,
            tertiary = info,
            onTertiary = onStateColor,
            tertiaryContainer = surfaceVariant,
            onTertiaryContainer = textPrimary,
            error = error,
            onError = onStateColor,
            errorContainer = error.copy(alpha = 0.18f),
            onErrorContainer = textPrimary,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = textSecondary,
            outline = textMuted,
            outlineVariant = surfaceVariant,
            scrim = TnyxPalette.Black,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = onPrimary,
            surfaceTint = primary
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = surfaceVariant,
            onPrimaryContainer = textPrimary,
            secondary = accent,
            onSecondary = onPrimary,
            secondaryContainer = surfaceVariant,
            onSecondaryContainer = textPrimary,
            tertiary = info,
            onTertiary = onStateColor,
            tertiaryContainer = surfaceVariant,
            onTertiaryContainer = textPrimary,
            error = error,
            onError = onStateColor,
            errorContainer = error.copy(alpha = 0.12f),
            onErrorContainer = textPrimary,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = textSecondary,
            outline = textMuted,
            outlineVariant = surfaceVariant,
            scrim = TnyxPalette.Black,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = onPrimary,
            surfaceTint = primary
        )
    }
}
