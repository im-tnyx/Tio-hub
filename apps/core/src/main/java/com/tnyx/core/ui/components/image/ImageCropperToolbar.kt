package com.tnyx.core.ui.components.image

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.RotateLeft
import androidx.compose.material.icons.automirrored.outlined.RotateRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Flip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme
import java.util.Locale
import kotlin.math.abs

internal const val ImageCropperRulerTestTag = "image_cropper_ruler"

@Composable
internal fun ImageCropperToolbar(
    fineAngle: Float,
    isFlippedHorizontal: Boolean,
    selectedAspectRatio: CropAspectRatio,
    isProcessing: Boolean,
    canApply: Boolean,
    onFineAngleChange: (Float) -> Unit,
    onFlip: () -> Unit,
    onRotateQuarterTurn: () -> Unit,
    onReset: () -> Unit,
    onAspectRatioSelected: (CropAspectRatio) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TnyxTheme.colors
    val dimens = TnyxTheme.dimens
    val tokens = TnyxTheme.components.imageCropper
    val primaryContentColor = colors.textPrimary
    val secondaryContentColor = colors.textSecondary
    val mutedContentColor = colors.textMuted
    val aspectAccentColor = if (selectedAspectRatio == CropAspectRatio.SQUARE) colors.info else mutedContentColor

    Column(
        modifier = modifier.background(colors.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = tokens.toolbarHorizontalPadding,
                    vertical = tokens.toolbarVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onFlip) {
                Icon(
                    imageVector = Icons.Outlined.Flip,
                    contentDescription = "Horizontal Flip / Mirror",
                    tint = if (isFlippedHorizontal) colors.primary else primaryContentColor,
                    modifier = Modifier.size(tokens.toolbarIconSize),
                )
            }

            HorizontalRulerDial(
                angle = fineAngle,
                onAngleChange = onFineAngleChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = tokens.rulerHorizontalPadding),
            )

            IconButton(onClick = onRotateQuarterTurn) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.RotateRight,
                    contentDescription = "Rotate 90 degrees",
                    tint = primaryContentColor,
                    modifier = Modifier.size(tokens.toolbarIconSize),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = tokens.toolbarSectionHorizontalPadding,
                    vertical = tokens.toolbarSectionVerticalPadding,
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onReset)
                    .padding(dimens.SpaceS),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.RotateLeft,
                    contentDescription = "Reset",
                    tint = primaryContentColor,
                    modifier = Modifier.size(tokens.toolbarIconSize),
                )
                Spacer(modifier = Modifier.height(dimens.SpaceXS))
                Text(
                    text = "Reset",
                    style = TnyxTheme.typography.labelSmall.copy(
                        color = secondaryContentColor,
                    ),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(tokens.aspectSelectorRadius))
                    .clickable { onAspectRatioSelected(CropAspectRatio.SQUARE) }
                    .padding(dimens.SpaceXS),
            ) {
                Box(
                    modifier = Modifier
                        .size(
                            width = tokens.aspectSelectorWidth,
                            height = tokens.aspectSelectorHeight,
                        )
                        .background(
                            color = colors.surfaceContainerHigh,
                            shape = RoundedCornerShape(tokens.aspectContainerRadius),
                        )
                        .border(
                            width = tokens.aspectBorderWidth,
                            color = aspectAccentColor,
                            shape = RoundedCornerShape(tokens.aspectContainerRadius),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(tokens.aspectPreviewSize)
                            .background(
                                primaryContentColor,
                                RoundedCornerShape(tokens.aspectPreviewRadius),
                            ),
                    )
                }
                Spacer(modifier = Modifier.height(dimens.SpaceXS))
                Text(
                    text = CropAspectRatio.SQUARE.label,
                    style = TnyxTheme.typography.labelMedium.copy(
                        color = aspectAccentColor,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = tokens.footerHorizontalPadding,
                    vertical = tokens.footerVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Cancel",
                    tint = primaryContentColor,
                    modifier = Modifier.size(tokens.actionIconSize),
                )
            }

            Text(
                text = "TRANSFORM",
                style = TnyxTheme.typography.titleMedium.copy(
                    color = primaryContentColor,
                    fontWeight = FontWeight.ExtraBold,
                ),
            )

            IconButton(
                enabled = canApply && !isProcessing,
                onClick = onApply,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Apply",
                    tint = if (isProcessing) mutedContentColor else colors.primary,
                    modifier = Modifier.size(tokens.actionIconSize),
                )
            }
        }
    }
}

@Composable
private fun HorizontalRulerDial(
    angle: Float,
    onAngleChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentAngle by rememberUpdatedState(angle)
    val currentOnAngleChange by rememberUpdatedState(onAngleChange)
    val tickColor = TnyxTheme.colors.textPrimary
    val dimens = TnyxTheme.dimens
    val tokens = TnyxTheme.components.imageCropper
    val overlayColor = TnyxTheme.colors.background.copy(alpha = tokens.rulerOverlayAlpha)

    Box(
        modifier = modifier
            .height(tokens.rulerHeight)
            .testTag(ImageCropperRulerTestTag)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val deltaAngle = -dragAmount.x * 0.05f
                    currentOnAngleChange((currentAngle + deltaAngle).coerceIn(-45f, 45f))
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val tickStepPx = tokens.rulerTickStep.toPx()

            for (index in -45..45) {
                val tickX = centerX + (index - currentAngle) * (tickStepPx / 3f)
                if (tickX in 0f..size.width) {
                    val distanceFromCenter = abs(tickX - centerX)
                    val alpha = (1f - (distanceFromCenter / (size.width / 2f))).coerceIn(
                        tokens.rulerMinimumTickAlpha,
                        1f,
                    )

                    if (index % 5 == 0) {
                        drawLine(
                            color = tickColor.copy(alpha = alpha * tokens.rulerMajorTickAlpha),
                            start = Offset(tickX, centerY - tokens.rulerMajorTickHalfHeight.toPx()),
                            end = Offset(tickX, centerY + tokens.rulerMajorTickHalfHeight.toPx()),
                            strokeWidth = tokens.rulerStrokeWidth.toPx(),
                        )
                    } else {
                        drawCircle(
                            color = tickColor.copy(alpha = alpha * tokens.rulerMinorTickAlpha),
                            radius = tokens.rulerDotRadius.toPx(),
                            center = Offset(tickX, centerY),
                        )
                    }
                }
            }
        }

        Surface(
            color = overlayColor,
            shape = TnyxTheme.shapes.Material.extraSmall,
            modifier = Modifier.padding(
                horizontal = dimens.SpaceXS,
                vertical = dimens.SpaceXXS,
            ),
        ) {
            Text(
                text = String.format(Locale.US, "%.1f", currentAngle),
                style = TnyxTheme.typography.bodySmall.copy(
                    color = tickColor,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.padding(
                    horizontal = dimens.SpaceS,
                    vertical = dimens.SpaceXXS,
                ),
            )
        }
    }
}
