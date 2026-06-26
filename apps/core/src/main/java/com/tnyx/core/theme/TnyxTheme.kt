package com.tnyx.core.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.tnyx.core.theme.locals.*
import com.tnyx.core.theme.tokens.components.*
import com.tnyx.core.theme.tokens.effects.*
import com.tnyx.core.theme.tokens.foundation.*
import com.tnyx.core.theme.tokens.semantic.*
import com.tnyx.core.theme.tokens.typography.TnyxTextStyles

/**
 * Tnyx Design System - Unified Accessor
 * This is the public API to consume theme tokens.
 */
object TnyxTheme {
    val colors: TnyxColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTnyxColors.current

    val dimens: TnyxDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalTnyxDimens.current

    val insets: TnyxInsets
        @Composable
        @ReadOnlyComposable
        get() = LocalTnyxInsets.current

    val elevation: TnyxElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalTnyxElevation.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalTnyxTypography.current

    val textStyles: TnyxTextStyles
        get() = TnyxTextStyles

    val motion: TnyxMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalTnyxMotion.current

    val shapes: TnyxShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalTnyxShapes.current

    val gradients: TnyxGradients
        @Composable
        @ReadOnlyComposable
        get() = LocalTnyxGradients.current

    val shadows: TnyxShadows
        @Composable
        @ReadOnlyComposable
        get() = LocalTnyxShadows.current

    val components: TnyxComponentTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalTnyxComponentTokens.current
}
