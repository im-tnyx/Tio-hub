package com.tnyx.core.ui.components.inputs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private const val CUPERTINO_DIAMETER_RATIO = 1.07f
private const val DATE_COLUMN_WEIGHT = 2.1f
private const val TIME_COLUMN_WEIGHT = 0.7f
private const val MERIDIEM_COLUMN_WEIGHT = 0.9f

@Composable
fun CupertinoDateTimePicker(
    visible: Boolean,
    initialDateTime: LocalDateTime,
    minimumDateTime: LocalDateTime,
    maximumDateTime: LocalDateTime,
    onDismissRequest: () -> Unit,
    onDateTimeChanged: (LocalDateTime) -> Unit,
) {
    require(!minimumDateTime.isAfter(maximumDateTime)) {
        "minimumDateTime must not be after maximumDateTime"
    }
    if (!visible) return

    val boundedInitial = initialDateTime.coerceIn(minimumDateTime, maximumDateTime)
    val dates = remember(minimumDateTime, maximumDateTime) {
        generateSequence(minimumDateTime.toLocalDate()) { date ->
            date.plusDays(1).takeIf { !it.isAfter(maximumDateTime.toLocalDate()) }
        }.toList()
    }
    val dateLabels = remember(dates) {
        dates.map { date -> date.format(DateTimeFormatter.ofPattern("EEE MMM d", Locale.US)) }
    }
    val hours = remember { (1..12).map(Int::toString) }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }
    val meridiems = remember { listOf("AM", "PM") }

    var dateIndex by remember(dates, boundedInitial) {
        mutableIntStateOf(dates.indexOf(boundedInitial.toLocalDate()).coerceAtLeast(0))
    }
    var hourIndex by remember(boundedInitial) { mutableIntStateOf(boundedInitial.hour.toTwelveHour() - 1) }
    var minuteIndex by remember(boundedInitial) { mutableIntStateOf(boundedInitial.minute) }
    var meridiemIndex by remember(boundedInitial) {
        mutableIntStateOf(if (boundedInitial.hour < 12) 0 else 1)
    }

    LaunchedEffect(dateIndex, hourIndex, minuteIndex, meridiemIndex) {
        val selectedHour = hours[hourIndex].toInt()
        val hour24 = when {
            meridiemIndex == 0 && selectedHour == 12 -> 0
            meridiemIndex == 1 && selectedHour != 12 -> selectedHour + 12
            else -> selectedHour
        }
        onDateTimeChanged(
            LocalDateTime.of(dates[dateIndex], LocalTime.of(hour24, minuteIndex))
                .coerceIn(minimumDateTime, maximumDateTime),
        )
    }

    TnyxModalBottomSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
        showDivider = false,
        contentBottomPadding = TnyxTheme.dimens.SpaceS,
    ) {
        val pickerHeight = TnyxTheme.dimens.CupertinoPickerHeight
        val itemHeight = TnyxTheme.dimens.CupertinoPickerItemHeight
        val sheetColor = TnyxTheme.components.sheet.containerColor

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pickerHeight)
                .clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(
                        color = TnyxTheme.colors.surfaceVariant,
                        shape = RoundedCornerShape(TnyxTheme.dimens.RadiusL),
                    ),
            )
            Row(modifier = Modifier.fillMaxSize()) {
                CupertinoWheelColumn(
                    items = dateLabels,
                    selectedIndex = dateIndex,
                    pickerHeight = pickerHeight,
                    itemHeight = itemHeight,
                    onItemSelected = { dateIndex = it },
                    modifier = Modifier.weight(DATE_COLUMN_WEIGHT),
                )
                CupertinoWheelColumn(
                    items = hours,
                    selectedIndex = hourIndex,
                    pickerHeight = pickerHeight,
                    itemHeight = itemHeight,
                    onItemSelected = { hourIndex = it },
                    modifier = Modifier.weight(TIME_COLUMN_WEIGHT),
                )
                CupertinoWheelColumn(
                    items = minutes,
                    selectedIndex = minuteIndex,
                    pickerHeight = pickerHeight,
                    itemHeight = itemHeight,
                    onItemSelected = { minuteIndex = it },
                    modifier = Modifier.weight(TIME_COLUMN_WEIGHT),
                )
                CupertinoWheelColumn(
                    items = meridiems,
                    selectedIndex = meridiemIndex,
                    pickerHeight = pickerHeight,
                    itemHeight = itemHeight,
                    onItemSelected = { meridiemIndex = it },
                    modifier = Modifier.weight(MERIDIEM_COLUMN_WEIGHT),
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to sheetColor,
                                0.20f to sheetColor.copy(alpha = 0.88f),
                                0.38f to Color.Transparent,
                                0.62f to Color.Transparent,
                                0.80f to sheetColor.copy(alpha = 0.88f),
                                1.0f to sheetColor,
                            ),
                        ),
                    ),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CupertinoWheelColumn(
    items: List<String>,
    selectedIndex: Int,
    pickerHeight: Dp,
    itemHeight: Dp,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialFirstVisibleIndex = (selectedIndex - 3).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialFirstVisibleIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val centreIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val itemsInfo = layoutInfo.visibleItemsInfo
            if (itemsInfo.isEmpty()) return@derivedStateOf selectedIndex
            val centre = layoutInfo.viewportStartOffset +
                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            itemsInfo.minByOrNull { item -> abs(item.offset + item.size / 2 - centre) }?.index
                ?: selectedIndex
        }
    }

    LaunchedEffect(centreIndex) { onItemSelected(centreIndex) }

    Box(
        modifier = modifier
            .height(pickerHeight)
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapBehavior,
            contentPadding = PaddingValues(vertical = (pickerHeight - itemHeight) / 2),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = index == centreIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .graphicsLayer {
                            val info = listState.layoutInfo.visibleItemsInfo
                                .firstOrNull { visibleItem -> visibleItem.index == index }
                            if (info != null) {
                                val viewportCentre = listState.layoutInfo.viewportStartOffset +
                                    listState.layoutInfo.viewportSize.height / 2f
                                val distance = info.offset + info.size / 2f - viewportCentre
                                val radius = listState.layoutInfo.viewportSize.height /
                                    (CUPERTINO_DIAMETER_RATIO * 2f)
                                val angle = (distance / radius).coerceIn(-1f, 1f)
                                rotationX = angle * 62f
                                scaleX = 1f - (0.12f * abs(angle))
                                scaleY = 1f - (0.18f * abs(angle))
                                alpha = (1f - (0.70f * abs(angle))).coerceIn(0f, 1f)
                                cameraDistance = 24f * density
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item,
                        style = TnyxTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) TnyxTheme.colors.textPrimary else TnyxTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private fun Int.toTwelveHour(): Int = when (val value = this % 12) {
    0 -> 12
    else -> value
}
