package com.tnyx.core.theme.tokens.foundation

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tnyx Layout & SafeArea Tokens
 */
data class TnyxInsets(
    val screenHorizontal: Dp,
    val screenVertical: Dp,
    val bottomNavPadding: Dp,
    val sheetHorizontal: Dp,
    val cardContent: Dp,
    val itemSpacing: Dp
)

val DefaultTnyxInsets = TnyxInsets(
    screenHorizontal = 16.dp,
    screenVertical = 16.dp,
    bottomNavPadding = 80.dp,
    sheetHorizontal = 20.dp,
    cardContent = 16.dp,
    itemSpacing = 12.dp
)
