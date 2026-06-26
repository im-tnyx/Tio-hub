package com.tnyx.core.theme.tokens.foundation

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * Tnyx Shape Primitives
 */
object TnyxShapes {
    val RadiusXS = TnyxDimens.RadiusXS
    val RadiusS = TnyxDimens.RadiusS
    val RadiusM = TnyxDimens.RadiusM
    val RadiusL = TnyxDimens.RadiusL
    val RadiusXL = TnyxDimens.RadiusXL
    val Radius2XL = TnyxDimens.Radius2XL
    val Radius3XL = TnyxDimens.Radius3XL
    val RadiusCircle = TnyxDimens.RadiusFull

    val Material = Shapes(
        extraSmall = RoundedCornerShape(RadiusXS),
        small = RoundedCornerShape(RadiusS),
        medium = RoundedCornerShape(RadiusXL),
        large = RoundedCornerShape(RadiusL),
        extraLarge = RoundedCornerShape(RadiusXL)
    )

    val Circle = CircleShape
    val None = RoundedCornerShape(TnyxDimens.SpaceNone)
}
