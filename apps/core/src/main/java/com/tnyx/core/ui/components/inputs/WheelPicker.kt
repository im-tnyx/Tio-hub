package com.tnyx.core.ui.components.inputs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(TnyxTheme.dimens.RadiusM),
    activeTextStyle: TextStyle = MaterialTheme.typography.titleLarge,
    inactiveTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    activeTextColor: Color = MaterialTheme.colorScheme.onBackground,
    inactiveTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    showSelectionHighlight: Boolean = true,
    usePerspective: Boolean = true,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val itemHeight = TnyxTheme.dimens.ButtonHeight
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val haptic = LocalHapticFeedback.current

    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf initialIndex

            val center = layoutInfo.viewportStartOffset +
                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            var closestIndex = initialIndex
            var minDistance = Int.MAX_VALUE

            for (itemInfo in visibleItems) {
                val itemCenter = itemInfo.offset + itemInfo.size / 2
                val distance = abs(itemCenter - center)
                if (distance < minDistance) {
                    minDistance = distance
                    closestIndex = itemInfo.index
                }
            }
            closestIndex
        }
    }

    var lastHapticIndex by remember { mutableIntStateOf(initialIndex) }

    LaunchedEffect(centerIndex) {
        onItemSelected(centerIndex)
        if (centerIndex != lastHapticIndex) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastHapticIndex = centerIndex
        }
    }

    Box(
        modifier = modifier.height(itemHeight * 5),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight * 2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(items) { index, item ->
                val isCenter = index == centerIndex
                val textStyle = if (isCenter) activeTextStyle else inactiveTextStyle
                val textColor = if (isCenter) activeTextColor else inactiveTextColor
                val fontWeight = if (isCenter) FontWeight.SemiBold else FontWeight.Normal

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .graphicsLayer {
                            if (usePerspective) {
                                val layoutInfo = listState.layoutInfo
                                val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == index }

                                if (visibleItem != null) {
                                    val centerOfViewport = layoutInfo.viewportStartOffset +
                                        (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2f
                                    val itemCenter = visibleItem.offset + visibleItem.size / 2f
                                    val distance = itemCenter - centerOfViewport
                                    val halfHeight = layoutInfo.viewportSize.height / 2f
                                    val fraction = (distance / halfHeight).coerceIn(-1f, 1f)

                                    rotationX = fraction * 70f

                                    val pullThreshold = 0.08f
                                    if (abs(fraction) > pullThreshold) {
                                        val pullFactor = (abs(fraction) - pullThreshold) / (1f - pullThreshold)
                                        translationY = -distance * pullFactor * 0.25f
                                    } else {
                                        translationY = 0f
                                    }

                                    val scale = 1f - (0.25f * abs(fraction))
                                    scaleX = scale
                                    scaleY = scale
                                    cameraDistance = 8f * density
                                    alpha = (1f - (0.8f * abs(fraction))).coerceIn(0f, 1f)
                                }
                            } else {
                                rotationX = 0f
                                translationY = 0f
                                scaleX = 1f
                                scaleY = 1f
                                alpha = 1f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (showSelectionHighlight) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight)
                                .background(
                                    color = if (isCenter) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = shape
                                )
                        )
                    }

                    Text(
                        text = item,
                        style = textStyle,
                        fontWeight = fontWeight,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
