package com.tnyx.core.theme.locals

import androidx.compose.runtime.staticCompositionLocalOf
import com.tnyx.core.theme.tokens.semantic.TnyxColors
import com.tnyx.core.theme.tokens.semantic.TnyxLightColors

val LocalTnyxColors = staticCompositionLocalOf<TnyxColors> {
    TnyxLightColors
}
