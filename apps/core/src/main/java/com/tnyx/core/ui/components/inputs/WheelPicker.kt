package com.tnyx.core.ui.components.inputs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    shape: Shape = RoundedCornerShape(TnyxTheme.dimens.RadiusM)
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

            val center = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
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

                val textStyle = if (isCenter) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.bodyMedium
                }

                val textColor = if (isCenter) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }

                val fontWeight = if (isCenter) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .graphicsLayer {
                            val layoutInfo = listState.layoutInfo
                            val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == index }

                            if (visibleItem != null) {
                                val centerOfViewport = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2f
                                val itemCenter = visibleItem.offset + visibleItem.size / 2f
                                val distance = itemCenter - centerOfViewport

                                val halfHeight = layoutInfo.viewportSize.height / 2f
                                val fraction = (distance / halfHeight).coerceIn(-1f, 1f)

                                rotationX = fraction * 70f

                                val pullThreshold = .08f
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
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // 🔥 Background highlight shape updated to match theme
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .background(
                                color = if (isCenter) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else Color.Transparent,
                                shape = shape
                            )
                    )

                    Text(
                        text = item,
                        style = textStyle,
                        fontWeight = if (isCenter) FontWeight.SemiBold
                        else FontWeight.Normal,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
