package com.tnyx.core.theme.tokens.foundation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing

/**
 * Tnyx Animation & Motion Tokens
 */
data class TnyxMotion(
    val DurationInstant: Int = 0,
    val DurationShort1: Int = 100,
    val DurationShort2: Int = 200,
    val DurationMedium1: Int = 300,
    val DurationMedium2: Int = 400,
    val DurationLong1: Int = 500,
    val DurationLong2: Int = 700,
    val DurationExtraLong: Int = 1000,
    val EasingLinear: Easing = LinearEasing,
    val EasingStandard: Easing = FastOutSlowInEasing,
    val EasingDecelerate: Easing = LinearOutSlowInEasing,
    val EasingAccelerate: Easing = FastOutLinearInEasing,
    val EasingEmphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
)

val DefaultTnyxMotion = TnyxMotion()

val ReducedTnyxMotion = TnyxMotion(
    DurationInstant = 0,
    DurationShort1 = 0,
    DurationShort2 = 0,
    DurationMedium1 = 0,
    DurationMedium2 = 0,
    DurationLong1 = 0,
    DurationLong2 = 0,
    DurationExtraLong = 0
)
