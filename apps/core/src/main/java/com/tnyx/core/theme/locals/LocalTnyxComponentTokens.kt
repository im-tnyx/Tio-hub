package com.tnyx.core.theme.locals

import androidx.compose.runtime.staticCompositionLocalOf
import com.tnyx.core.theme.tokens.components.*

val LocalTnyxComponentTokens = staticCompositionLocalOf {
    TnyxComponentTokens(
        button = DefaultButtonTokens,
        input = DefaultInputTokens,
        card = DefaultCardTokens,
        sheet = DefaultSheetTokens,
        navigation = NavigationTokens()
    )
}
