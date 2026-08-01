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
import androidx.compose.runtime.mutableStateOf
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
private const val WRAP_REPEAT_COUNT = 200

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

    var selectedDateTime: LocalDateTime by remember(minimumDateTime, maximumDateTime, boundedInitial) {
        mutableStateOf(boundedInitial)
    }
    var hourVirtualIndex by remember(boundedInitial) {
        mutableIntStateOf(wrappedMiddleIndex(hours.size, boundedInitial.hour.toTwelveHour() - 1))
    }
    var minuteVirtualIndex by remember(boundedInitial) {
        mutableIntStateOf(wrappedMiddleIndex(minutes.size, boundedInitial.minute))
    }

    val dateIndex = remember(dates, selectedDateTime) {
        dates.indexOf(selectedDateTime.toLocalDate()).coerceAtLeast(0)
    }
    val hourIndex = remember(selectedDateTime) {
        selectedDateTime.hour.toTwelveHour() - 1
    }
    val minuteIndex = remember(selectedDateTime) {
        selectedDateTime.minute
    }
    val meridiemIndex = remember(selectedDateTime) {
        if (selectedDateTime.hour < 12) 0 else 1
    }

    LaunchedEffect(selectedDateTime) {
        onDateTimeChanged(selectedDateTime.coerceIn(minimumDateTime, maximumDateTime))
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
                    onItemSelected = { index, _ ->
                        selectedDateTime = LocalDateTime.of(
                            dates[index],
                            selectedDateTime.toLocalTime(),
                        ).coerceIn(minimumDateTime, maximumDateTime)
                    },
                    itemTextAlign = TextAlign.End,
                    itemContentAlignment = Alignment.CenterEnd,
                    itemEndPadding = TnyxTheme.dimens.SpaceL,
                    modifier = Modifier.weight(DATE_COLUMN_WEIGHT),
                )
                CupertinoWheelColumn(
                    items = hours,
                    selectedIndex = hourIndex,
                    pickerHeight = pickerHeight,
                    itemHeight = itemHeight,
                    onItemSelected = { nextHourIndex, virtualIndex ->
                        if (virtualIndex == hourVirtualIndex) return@CupertinoWheelColumn
                        val direction = if (virtualIndex > hourVirtualIndex) 1 else -1
                        selectedDateTime = advanceToHour(
                            current = selectedDateTime,
                            targetHour12 = hours[nextHourIndex].toInt(),
                            direction = direction,
                        ).coerceIn(minimumDateTime, maximumDateTime)
                        hourVirtualIndex = virtualIndex
                    },
                    wrapAround = true,
                    itemStartPadding = TnyxTheme.dimens.SpaceXXS,
                    itemEndPadding = TnyxTheme.dimens.SpaceXXS,
                    modifier = Modifier.weight(TIME_COLUMN_WEIGHT),
                )
                CupertinoWheelColumn(
                    items = minutes,
                    selectedIndex = minuteIndex,
                    pickerHeight = pickerHeight,
                    itemHeight = itemHeight,
                    onItemSelected = { nextMinuteIndex, virtualIndex ->
                        if (virtualIndex == minuteVirtualIndex) return@CupertinoWheelColumn
                        val direction = if (virtualIndex > minuteVirtualIndex) 1 else -1
                        selectedDateTime = advanceToMinute(
                            current = selectedDateTime,
                            targetMinute = nextMinuteIndex,
                            direction = direction,
                        ).coerceIn(minimumDateTime, maximumDateTime)
                        minuteVirtualIndex = virtualIndex
                    },
                    wrapAround = true,
                    itemStartPadding = TnyxTheme.dimens.SpaceXXS,
                    itemEndPadding = TnyxTheme.dimens.SpaceXXS,
                    modifier = Modifier.weight(TIME_COLUMN_WEIGHT),
                )
                CupertinoMeridiemColumn(
                    items = meridiems,
                    selectedIndex = meridiemIndex,
                    pickerHeight = pickerHeight,
                    itemHeight = itemHeight,
                    onItemSelected = { index ->
                        val selectedHour = selectedDateTime.hour.toTwelveHour()
                        val hour24 = when {
                            index == 0 && selectedHour == 12 -> 0
                            index == 0 -> selectedHour
                            selectedHour == 12 -> 12
                            else -> selectedHour + 12
                        }
                        selectedDateTime = selectedDateTime
                            .withHour(hour24)
                            .coerceIn(minimumDateTime, maximumDateTime)
                    },
                    itemStartPadding = TnyxTheme.dimens.SpaceM,
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
private fun CupertinoMeridiemColumn(
    items: List<String>,
    selectedIndex: Int,
    pickerHeight: Dp,
    itemHeight: Dp,
    onItemSelected: (Int) -> Unit,
    itemStartPadding: Dp = TnyxTheme.dimens.SpaceNone,
    itemEndPadding: Dp = TnyxTheme.dimens.SpaceNone,
    modifier: Modifier = Modifier,
) {
    val boundedSelectedIndex = selectedIndex.coerceIn(0, items.lastIndex)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = boundedSelectedIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val centreIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val itemsInfo = layoutInfo.visibleItemsInfo
            if (itemsInfo.isEmpty()) return@derivedStateOf boundedSelectedIndex
            val centre = layoutInfo.viewportStartOffset +
                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            itemsInfo.minByOrNull { item -> abs(item.offset + item.size / 2 - centre) }?.index
                ?.coerceIn(0, items.lastIndex)
                ?: boundedSelectedIndex
        }
    }

    LaunchedEffect(boundedSelectedIndex) {
        if (listState.firstVisibleItemIndex != boundedSelectedIndex) {
            listState.scrollToItem(boundedSelectedIndex)
        }
    }
    LaunchedEffect(centreIndex) {
        if (centreIndex != boundedSelectedIndex) {
            onItemSelected(centreIndex)
        }
    }

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
                        .padding(start = itemStartPadding, end = itemEndPadding)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CupertinoWheelColumn(
    items: List<String>,
    selectedIndex: Int,
    pickerHeight: Dp,
    itemHeight: Dp,
    onItemSelected: (Int, Int) -> Unit,
    wrapAround: Boolean = false,
    itemTextAlign: TextAlign = TextAlign.Center,
    itemContentAlignment: Alignment = Alignment.Center,
    itemStartPadding: Dp = TnyxTheme.dimens.SpaceNone,
    itemEndPadding: Dp = TnyxTheme.dimens.SpaceNone,
    modifier: Modifier = Modifier,
) {
    val initialSelectedIndex = if (wrapAround) {
        wrappedMiddleIndex(items.size, selectedIndex)
    } else {
        selectedIndex
    }
    val initialFirstVisibleIndex = (initialSelectedIndex - 3).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialFirstVisibleIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val totalItemsCount = if (wrapAround) items.size * WRAP_REPEAT_COUNT else items.size
    val centreVirtualIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val itemsInfo = layoutInfo.visibleItemsInfo
            if (itemsInfo.isEmpty()) return@derivedStateOf initialSelectedIndex
            val centre = layoutInfo.viewportStartOffset +
                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            itemsInfo.minByOrNull { item -> abs(item.offset + item.size / 2 - centre) }?.index
                ?: initialSelectedIndex
        }
    }
    val centreActualIndex by remember {
        derivedStateOf {
            if (wrapAround) {
                centreVirtualIndex.wrapIndex(items.size)
            } else {
                centreVirtualIndex.coerceIn(0, items.lastIndex)
            }
        }
    }

    LaunchedEffect(centreActualIndex, centreVirtualIndex) {
        onItemSelected(centreActualIndex, centreVirtualIndex)
    }
    LaunchedEffect(centreVirtualIndex, wrapAround, totalItemsCount) {
        if (!wrapAround || items.size <= 1) return@LaunchedEffect
        val safetyCycles = 20
        val lowerBound = items.size * safetyCycles
        val upperBound = totalItemsCount - lowerBound
        if (centreVirtualIndex in lowerBound..upperBound) return@LaunchedEffect
        val rebasedIndex = wrappedMiddleIndex(items.size, centreActualIndex)
        listState.scrollToItem((rebasedIndex - 3).coerceAtLeast(0))
    }

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
            items(totalItemsCount) { virtualIndex ->
                val actualIndex = if (wrapAround) {
                    virtualIndex.wrapIndex(items.size)
                } else {
                    virtualIndex
                }
                val item = items[actualIndex]
                val isSelected = virtualIndex == centreVirtualIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .padding(start = itemStartPadding, end = itemEndPadding)
                        .graphicsLayer {
                            val info = listState.layoutInfo.visibleItemsInfo
                                .firstOrNull { visibleItem -> visibleItem.index == virtualIndex }
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
                    contentAlignment = itemContentAlignment,
                ) {
                    Text(
                        text = item,
                        style = TnyxTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) TnyxTheme.colors.textPrimary else TnyxTheme.colors.textSecondary,
                        textAlign = itemTextAlign,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private fun wrappedMiddleIndex(size: Int, selectedIndex: Int): Int {
    if (size <= 0) return 0
    val middleCycle = WRAP_REPEAT_COUNT / 2
    return (middleCycle * size) + selectedIndex.wrapIndex(size)
}

private fun Int.wrapIndex(size: Int): Int {
    if (size <= 0) return 0
    return ((this % size) + size) % size
}

private fun advanceToHour(
    current: LocalDateTime,
    targetHour12: Int,
    direction: Int,
): LocalDateTime {
    var candidate = current
    repeat(12) {
        if (candidate.hour.toTwelveHour() == targetHour12) {
            return candidate
        }
        candidate = if (direction >= 0) candidate.plusHours(1) else candidate.minusHours(1)
    }
    return candidate
}

private fun advanceToMinute(
    current: LocalDateTime,
    targetMinute: Int,
    direction: Int,
): LocalDateTime {
    var candidate = current
    repeat(60) {
        if (candidate.minute == targetMinute) {
            return candidate
        }
        candidate = if (direction >= 0) candidate.plusMinutes(1) else candidate.minusMinutes(1)
    }
    return candidate
}

private fun Int.toTwelveHour(): Int = when (val value = this % 12) {
    0 -> 12
    else -> value
}
