package com.tnyx.core.ui.components.inputs

import android.graphics.Paint as AndroidPaint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.navigation.TnyxTabItem
import com.tnyx.core.ui.components.navigation.TnyxTabSwitcher
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

enum class TnyxClockEditTarget { Sleep, Wake }

private const val SLEEP_CUPERTINO_DIAMETER_RATIO = 1.07f
private const val SLEEP_WRAP_REPEAT_COUNT = 200
private const val SLEEP_TIME_COLUMN_WEIGHT = 0.82f
private const val SLEEP_MERIDIEM_COLUMN_WEIGHT = 0.72f
private const val SLEEP_WHEEL_TEXT_SIZE = 24
private const val SLEEP_WHEEL_SEPARATOR_TEXT_SIZE = 26
private val SleepTargetTabs = listOf(
    TnyxTabItem(
        label = "Sleep",
        icon = Icons.Rounded.Bedtime,
        value = TnyxClockEditTarget.Sleep,
    ),
    TnyxTabItem(
        label = "Wake",
        icon = Icons.Rounded.Alarm,
        value = TnyxClockEditTarget.Wake,
    ),
)

/**
 * 1-to-1 Exact Kotlin Compose Replica of Flutter's `TnyxSleepWakeClockCard` & `TnyxSleepWakeWheelCard`.
 * Matches every single UI spec, interaction guard, haptic feedback, 24-hour circular painter,
 * center display rows, wheel picker columns, duration subtitle, and bottom sheet handle behavior.
 */
@Composable
fun SleepScheduleBottomSheet(
    visible: Boolean,
    sleepTime: String,
    wakeTime: String,
    onDismissRequest: () -> Unit,
    onSave: (sleepTime: String, wakeTime: String) -> Unit,
) {
    if (!visible) return

    var useWheelStyle by remember { mutableStateOf(false) }
    var selectedTarget by remember { mutableStateOf<TnyxClockEditTarget?>(null) }
    var wheelTarget by remember { mutableStateOf(TnyxClockEditTarget.Sleep) }
    var manualInputTarget by remember { mutableStateOf<TnyxClockEditTarget?>(null) }

    var bedTimeLocal by remember(sleepTime) { mutableStateOf(parseTime(sleepTime) ?: LocalTime.of(22, 0)) }
    var wakeTimeLocal by remember(wakeTime) { mutableStateOf(parseTime(wakeTime) ?: LocalTime.of(7, 0)) }

    val formattedBedTime = remember(bedTimeLocal) { formatTimeOfDay(bedTimeLocal) }
    val formattedWakeTime = remember(wakeTimeLocal) { formatTimeOfDay(wakeTimeLocal) }

    val sleepDurationMins = remember(bedTimeLocal, wakeTimeLocal) {
        calcSleepDurationMinutes(bedTimeLocal.toMinutes(), wakeTimeLocal.toMinutes())
    }

    val durationSubtitle = remember(sleepDurationMins) {
        "Sleep time: ${formatDuration(sleepDurationMins)}"
    }
    val ringAccentColor = TnyxTheme.colors.ai

    val manualInputTime = when (manualInputTarget) {
        TnyxClockEditTarget.Sleep -> bedTimeLocal
        TnyxClockEditTarget.Wake -> wakeTimeLocal
        null -> null
    }

    TnyxModalBottomSheet(
        visible = true,
        title = "Sleep schedule",
        closeOnBackdropClick = false,
        onDismissRequest = onDismissRequest,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AnimatedContent(
                targetState = useWheelStyle,
                label = "StyleSwitcher"
            ) { isWheel ->
                if (!isWheel) {
                    // ─── CLOCK DIAL VIEW (TnyxSleepWakeClockCard) ───
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = TnyxTheme.dimens.SpaceXS)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { useWheelStyle = true },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = "Wheel Style",
                                    tint = TnyxTheme.colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        // 24-Hour Circular Sleep Clock (AspectRatio 1)
                        SleepWakeClockDialWidget(
                            bedTime = bedTimeLocal,
                            wakeTime = wakeTimeLocal,
                            selectedTarget = selectedTarget,
                            formattedBedTime = formattedBedTime,
                            formattedWakeTime = formattedWakeTime,
                            onBedTimeChanged = { bedTimeLocal = it },
                            onWakeTimeChanged = { wakeTimeLocal = it },
                            onTargetChanged = { selectedTarget = it },
                            onSleepTap = {
                                selectedTarget = if (selectedTarget == TnyxClockEditTarget.Sleep) null else TnyxClockEditTarget.Sleep
                            },
                            onWakeTap = {
                                selectedTarget = if (selectedTarget == TnyxClockEditTarget.Wake) null else TnyxClockEditTarget.Wake
                            },
                            onSleepLabelTap = { manualInputTarget = TnyxClockEditTarget.Sleep },
                            onWakeLabelTap = { manualInputTarget = TnyxClockEditTarget.Wake }
                        )

                        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))

                        // Subtitle Duration aligned with the ring accent color
                        Text(
                            text = durationSubtitle,
                            style = TnyxTheme.typography.titleMedium,
                            fontWeight = FontWeight.W500,
                            color = ringAccentColor
                        )
                    }
                } else {
                    // ─── 3D WHEEL VIEW (TnyxSleepWakeWheelCard) ───
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = TnyxTheme.dimens.SpaceXS)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            TnyxTabSwitcher(
                                tabs = SleepTargetTabs,
                                selectedValue = wheelTarget,
                                onTabSelected = { wheelTarget = it },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .width(188.dp),
                            )

                            IconButton(
                                onClick = { useWheelStyle = false },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccessTime,
                                    contentDescription = "Clock Dial Style",
                                    tint = TnyxTheme.colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        // AspectRatio 1 Square Container for Wheel Picker (Height 160)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            val activeTargetEnum = wheelTarget
                            if (activeTargetEnum == TnyxClockEditTarget.Sleep) {
                                CupertinoTimeWheelPicker(
                                    initialTime = bedTimeLocal,
                                    onTimeSelected = { bedTimeLocal = it }
                                )
                            } else {
                                CupertinoTimeWheelPicker(
                                    initialTime = wakeTimeLocal,
                                    onTimeSelected = { wakeTimeLocal = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))

                        Text(
                            text = durationSubtitle,
                            style = TnyxTheme.typography.titleMedium,
                            fontWeight = FontWeight.W500,
                            color = ringAccentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))

            TnyxPrimaryButton(
                text = "Save Schedule",
                onPressed = { onSave(formattedBedTime, formattedWakeTime) },
                expand = true,
            )
        }
    }

    if (manualInputTarget != null && manualInputTime != null) {
        SleepScheduleTimeInputDialog(
            target = manualInputTarget!!,
            initialTime = manualInputTime,
            onDismiss = { manualInputTarget = null },
            onConfirm = { newTime ->
                if (manualInputTarget == TnyxClockEditTarget.Sleep) {
                    bedTimeLocal = newTime
                } else {
                    wakeTimeLocal = newTime
                }
                manualInputTarget = null
            }
        )
    }
}

@Composable
private fun SleepWakeClockDialWidget(
    bedTime: LocalTime,
    wakeTime: LocalTime,
    selectedTarget: TnyxClockEditTarget?,
    formattedBedTime: String,
    formattedWakeTime: String,
    onBedTimeChanged: (LocalTime) -> Unit,
    onWakeTimeChanged: (LocalTime) -> Unit,
    onTargetChanged: (TnyxClockEditTarget?) -> Unit,
    onSleepTap: () -> Unit,
    onWakeTap: () -> Unit,
    onSleepLabelTap: () -> Unit,
    onWakeLabelTap: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val textPrimaryColor = TnyxTheme.colors.textPrimary
    val baseTrackColor = textPrimaryColor.copy(alpha = 0.10f)
    val ringAccentColor = TnyxTheme.colors.ai
    val ringAccentContentColor = if (TnyxTheme.colors.isDark) {
        TnyxTheme.colors.textPrimary
    } else {
        TnyxTheme.colors.onPrimary
    }
    val markerInactiveIconColor = TnyxTheme.colors.accent

    var pressedTarget by remember { mutableStateOf<TnyxClockEditTarget?>(null) }
    var draggingTarget by remember { mutableStateOf<TnyxClockEditTarget?>(null) }
    var lastHapticMins by remember { mutableStateOf<Int?>(null) }

    val sleepMins = bedTime.toMinutes()
    val wakeMins = wakeTime.toMinutes()
    val currentDisplayTarget = draggingTarget ?: pressedTarget ?: selectedTarget

    val latestSleepMins by rememberUpdatedState(sleepMins)
    val latestWakeMins by rememberUpdatedState(wakeMins)
    val latestOnBedTimeChanged by rememberUpdatedState(onBedTimeChanged)
    val latestOnWakeTimeChanged by rememberUpdatedState(onWakeTimeChanged)
    val latestOnTargetChanged by rememberUpdatedState(onTargetChanged)
    val latestOnSleepTap by rememberUpdatedState(onSleepTap)
    val latestOnWakeTap by rememberUpdatedState(onWakeTap)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.TopStart
    ) {
        val density = LocalDensity.current
        val widthDp = maxWidth
        val heightDp = maxHeight
        val widthPx = with(density) { widthDp.toPx() }
        val heightPx = with(density) { heightDp.toPx() }
        val centerPx = Offset(widthPx / 2f, heightPx / 2f)
        val ringRadiusPx = minOf(widthPx, heightPx) / 2f - with(density) { 15.dp.toPx() }
        val ringRadiusDp = with(density) { ringRadiusPx.toDp() }
        val centerDp = Offset(widthDp.value / 2f, heightDp.value / 2f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(centerPx, ringRadiusPx) {
                    fun resolveTarget(pointerPosition: Offset): TnyxClockEditTarget? {
                        val sleepPosition = getMarkerOffsetPx(centerPx, ringRadiusPx, latestSleepMins)
                        val wakePosition = getMarkerOffsetPx(centerPx, ringRadiusPx, latestWakeMins)
                        val sleepDistance = (pointerPosition - sleepPosition).getDistance()
                        val wakeDistance = (pointerPosition - wakePosition).getDistance()

                        return when {
                            sleepDistance < wakeDistance && sleepDistance < 60.dp.toPx() -> TnyxClockEditTarget.Sleep
                            wakeDistance < 60.dp.toPx() -> TnyxClockEditTarget.Wake
                            else -> null
                        }
                    }

                    fun updateDraggedTime(target: TnyxClockEditTarget, pointerPosition: Offset) {
                        val touchOffset = pointerPosition - centerPx
                        var angle = atan2(touchOffset.y.toDouble(), touchOffset.x.toDouble()) + Math.PI / 2
                        if (angle < 0) angle += 2 * Math.PI

                        val rawMins = (angle / (2 * Math.PI) * (24 * 60)).roundToInt()
                        val snappedMins = (rawMins / 10.0).roundToInt() * 10 % (24 * 60)

                        if (target == TnyxClockEditTarget.Sleep) {
                            val currentDuration = calcSleepDurationMinutes(latestSleepMins, latestWakeMins)
                            val newDuration = calcSleepDurationMinutes(snappedMins, latestWakeMins)
                            if (abs(newDuration - currentDuration) > 12 * 60) return
                            if (newDuration < 5 || newDuration > 1435) return

                            if (snappedMins != lastHapticMins) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastHapticMins = snappedMins
                            }

                            latestOnBedTimeChanged(fromMinutes(snappedMins))
                        } else {
                            val currentDuration = calcSleepDurationMinutes(latestSleepMins, latestWakeMins)
                            val newDuration = calcSleepDurationMinutes(latestSleepMins, snappedMins)
                            if (abs(newDuration - currentDuration) > 12 * 60) return
                            if (newDuration < 5 || newDuration > 1435) return

                            if (snappedMins != lastHapticMins) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastHapticMins = snappedMins
                            }

                            latestOnWakeTimeChanged(fromMinutes(snappedMins))
                        }
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val target = resolveTarget(down.position) ?: return@awaitEachGesture

                        pressedTarget = target
                        var didDrag = false
                        var released = false
                        val startPosition = down.position

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break

                            if (!change.pressed) {
                                released = true
                                break
                            }

                            if (!didDrag && (change.position - startPosition).getDistance() > viewConfiguration.touchSlop) {
                                didDrag = true
                                pressedTarget = null
                                draggingTarget = target
                                latestOnTargetChanged(target)
                            }

                            if (didDrag) {
                                updateDraggedTime(target, change.position)
                                change.consume()
                            }
                        }

                        pressedTarget = null
                        draggingTarget = null
                        lastHapticMins = null

                        if (didDrag) {
                            latestOnTargetChanged(null)
                        } else if (released) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (target == TnyxClockEditTarget.Sleep) {
                                latestOnSleepTap()
                            } else {
                                latestOnWakeTap()
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
            val trackWidth = 30.dp.toPx()

            // 1. Base Dark Track Ring
            drawCircle(
                color = baseTrackColor,
                radius = ringRadiusPx,
                center = centerPx,
                style = Stroke(width = trackWidth)
            )

            // 2. Active Sleep Arc Track (#7A8BE8)
            val sleepAngleDeg = (sleepMins / 1440f) * 360f - 90f
            val normWake = if (wakeMins <= sleepMins) wakeMins + 1440 else wakeMins
            val sweepAngleDeg = ((normWake - sleepMins) / 1440f) * 360f

            drawArc(
                color = ringAccentColor,
                startAngle = sleepAngleDeg,
                sweepAngle = sweepAngleDeg,
                useCenter = false,
                topLeft = Offset(centerPx.x - ringRadiusPx, centerPx.y - ringRadiusPx),
                size = Size(ringRadiusPx * 2, ringRadiusPx * 2),
                style = Stroke(width = trackWidth, cap = StrokeCap.Round)
            )

            // 3. 144 White Dots along active arc
            for (i in 0 until 144) {
                val dotAngleRad = (i / 144f) * 2 * Math.PI - Math.PI / 2
                val dotAngleDeg = Math.toDegrees(dotAngleRad).toFloat()

                var normDot = (dotAngleDeg - sleepAngleDeg) % 360f
                if (normDot < 0) normDot += 360f

                if (normDot <= sweepAngleDeg + 0.5f) {
                    val px = centerPx.x + ringRadiusPx * cos(dotAngleRad).toFloat()
                    val py = centerPx.y + ringRadiusPx * sin(dotAngleRad).toFloat()
                    drawCircle(
                        color = Color.White.copy(alpha = 0.82f),
                        radius = 1.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }

            // 4. Inner Minute Ticks (144 ticks, skip at 0, 6, 12, 18)
            val tickGap = 20.dp.toPx()
            for (i in 0 until 144) {
                if (i % 36 == 0) continue // Skip tick at hour label positions (0, 6, 12, 18)
                val angleRad = (i / 144f) * 2 * Math.PI - Math.PI / 2
                val isMajor = i % 6 == 0
                val tickLength = if (isMajor) 5.dp.toPx() else 4.dp.toPx()

                val startP = Offset(
                    centerPx.x + (ringRadiusPx - tickGap) * cos(angleRad).toFloat(),
                    centerPx.y + (ringRadiusPx - tickGap) * sin(angleRad).toFloat()
                )
                val endP = Offset(
                    centerPx.x + (ringRadiusPx - tickGap - tickLength) * cos(angleRad).toFloat(),
                    centerPx.y + (ringRadiusPx - tickGap - tickLength) * sin(angleRad).toFloat()
                )

                drawLine(
                    color = textPrimaryColor.copy(alpha = if (isMajor) 0.30f else 0.12f),
                    start = startP,
                    end = endP,
                    strokeWidth = if (isMajor) 1.1.dp.toPx() else 1.dp.toPx()
                )
            }

            // 5. Hour Text Labels (0, 6, 12, 18)
            val labelRadius = ringRadiusPx - 25.dp.toPx()
            val textPaint = AndroidPaint().apply {
                color = textPrimaryColor.copy(alpha = 0.60f).toArgb()
                textSize = 14.sp.toPx()
                isFakeBoldText = true
                textAlign = AndroidPaint.Align.CENTER
            }

            val labels = listOf("0" to -Math.PI / 2, "6" to 0.0, "12" to Math.PI / 2, "18" to Math.PI)
            for ((text, angleRad) in labels) {
                val lx = centerPx.x + labelRadius * cos(angleRad).toFloat()
                val ly = centerPx.y + labelRadius * sin(angleRad).toFloat() + (textPaint.textSize / 3f)
                drawContext.canvas.nativeCanvas.drawText(text, lx, ly, textPaint)
            }
            }

            // 6. Bedtime Marker Handle (Moon)
            val bedMarkerOffsetDp = getMarkerOffsetDp(centerDp, ringRadiusDp.value, sleepMins)
            MarkerHandle(
                icon = Icons.Rounded.Bedtime,
                isSelected = currentDisplayTarget == TnyxClockEditTarget.Sleep,
                isPressed = pressedTarget == TnyxClockEditTarget.Sleep || draggingTarget == TnyxClockEditTarget.Sleep,
                offset = DpOffset(bedMarkerOffsetDp.x.dp, bedMarkerOffsetDp.y.dp),
                accentColor = ringAccentColor,
                activeIconColor = ringAccentContentColor,
                inactiveIconColor = markerInactiveIconColor,
            )

            // 7. Wake-up Marker Handle (Alarm)
            val wakeMarkerOffsetDp = getMarkerOffsetDp(centerDp, ringRadiusDp.value, wakeMins)
            MarkerHandle(
                icon = Icons.Rounded.Alarm,
                isSelected = currentDisplayTarget == TnyxClockEditTarget.Wake,
                isPressed = pressedTarget == TnyxClockEditTarget.Wake || draggingTarget == TnyxClockEditTarget.Wake,
                offset = DpOffset(wakeMarkerOffsetDp.x.dp, wakeMarkerOffsetDp.y.dp),
                accentColor = ringAccentColor,
                activeIconColor = ringAccentContentColor,
                inactiveIconColor = markerInactiveIconColor,
            )

            // 8. Center Content Display (Flutter _ClockCenterRow Column)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.Center)
            ) {
                if (currentDisplayTarget == null || currentDisplayTarget == TnyxClockEditTarget.Sleep) {
                    val isSleepActive = currentDisplayTarget == TnyxClockEditTarget.Sleep
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onSleepLabelTap() }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bedtime,
                            contentDescription = null,
                            tint = if (isSleepActive) ringAccentColor else TnyxTheme.colors.textSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = formattedBedTime,
                            style = TnyxTheme.typography.titleLarge,
                            fontWeight = FontWeight.W500,
                            color = if (isSleepActive) ringAccentColor else textPrimaryColor
                        )
                    }
                }

                if (currentDisplayTarget == null) {
                    Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceSM))
                }

                if (currentDisplayTarget == null || currentDisplayTarget == TnyxClockEditTarget.Wake) {
                    val isWakeActive = currentDisplayTarget == TnyxClockEditTarget.Wake
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onWakeLabelTap() }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Alarm,
                            contentDescription = null,
                            tint = if (isWakeActive) ringAccentColor else TnyxTheme.colors.textSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = formattedWakeTime,
                            style = TnyxTheme.typography.titleLarge,
                            fontWeight = FontWeight.W500,
                            color = if (isWakeActive) ringAccentColor else textPrimaryColor
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepScheduleTimeInputDialog(
    target: TnyxClockEditTarget,
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    key(target, initialTime) {
        val pickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = false,
        )

        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(TnyxTheme.shapes.Material.medium)
                        .background(TnyxTheme.colors.surface)
                        .padding(TnyxTheme.dimens.SpaceL)
                ) {
                    Text(
                        text = if (target == TnyxClockEditTarget.Sleep) "Edit bedtime" else "Edit wake-up time",
                        style = TnyxTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TnyxTheme.colors.textPrimary,
                    )

                    Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

                    TimeInput(state = pickerState)

                    Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = "CANCEL",
                                color = TnyxTheme.colors.textPrimary,
                                style = TnyxTheme.typography.labelLarge
                            )
                        }
                        TextButton(
                            onClick = {
                                onConfirm(LocalTime.of(pickerState.hour, pickerState.minute))
                            }
                        ) {
                            Text(
                                text = "OK",
                                color = TnyxTheme.colors.primary,
                                style = TnyxTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.MarkerHandle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isPressed: Boolean,
    offset: DpOffset,
    accentColor: Color,
    activeIconColor: Color,
    inactiveIconColor: Color,
) {
    val active = isSelected || isPressed
    // Flutter: active ? 42.0 : 30.0
    val size: Dp = if (active) TnyxTheme.dimens.InputHeightCompact else 30.dp
    val iconSize: Dp = if (active) 22.dp else TnyxTheme.dimens.TabIconSize

    Box(
        modifier = Modifier
            .offset(x = offset.x - size / 2, y = offset.y - size / 2)
            .size(size)
            .border(
                width = if (active) 1.5.dp else TnyxTheme.dimens.BorderThin,
                color = if (active) accentColor else TnyxTheme.colors.surface.copy(alpha = 0.01f),
                shape = CircleShape,
            )
            .clip(CircleShape)
            .background(accentColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) activeIconColor else inactiveIconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

private fun getMarkerOffsetPx(centerPx: Offset, radiusPx: Float, minutes: Int): Offset {
    val angleRad = (minutes / (24 * 60f)) * 2 * Math.PI - Math.PI / 2
    return Offset(
        x = (centerPx.x + radiusPx * cos(angleRad)).toFloat(),
        y = (centerPx.y + radiusPx * sin(angleRad)).toFloat()
    )
}

private fun getMarkerOffsetDp(centerDp: Offset, radiusDp: Float, minutes: Int): Offset {
    val angleRad = (minutes / (24 * 60f)) * 2 * Math.PI - Math.PI / 2
    return Offset(
        x = (centerDp.x + radiusDp * cos(angleRad)).toFloat(),
        y = (centerDp.y + radiusDp * sin(angleRad)).toFloat()
    )
}

private fun LocalTime.toMinutes(): Int = hour * 60 + minute

private fun fromMinutes(totalMinutes: Int): LocalTime {
    val norm = ((totalMinutes % (24 * 60)) + (24 * 60)) % (24 * 60)
    return LocalTime.of(norm / 60, norm % 60)
}

private fun calcSleepDurationMinutes(sleepMinutes: Int, wakeMinutes: Int): Int {
    return if (wakeMinutes >= sleepMinutes) {
        wakeMinutes - sleepMinutes
    } else {
        (24 * 60 - sleepMinutes) + wakeMinutes
    }
}

private fun formatDuration(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (minutes == 0) {
        "$hours hours"
    } else {
        "$hours hours ${minutes.toString().padStart(2, '0')} minutes"
    }
}

@Composable
private fun CupertinoTimeWheelPicker(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    val hours = remember { (1..12).map { it.toString() } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }
    val meridiems = remember { listOf("AM", "PM") }

    var selectedTime by remember { mutableStateOf(initialTime) }
    var hourVirtualIndex by remember { mutableIntStateOf(sleepWrappedMiddleIndex(hours.size, initialTime.hour.toTwelveHour() - 1)) }
    var minuteVirtualIndex by remember { mutableIntStateOf(sleepWrappedMiddleIndex(minutes.size, initialTime.minute)) }

    LaunchedEffect(initialTime) {
        if (selectedTime != initialTime) {
            selectedTime = initialTime
            hourVirtualIndex = sleepWrappedMiddleIndex(hours.size, initialTime.hour.toTwelveHour() - 1)
            minuteVirtualIndex = sleepWrappedMiddleIndex(minutes.size, initialTime.minute)
        }
    }
    LaunchedEffect(selectedTime) {
        onTimeSelected(selectedTime)
    }

    val hourIndex = remember(selectedTime) {
        selectedTime.hour.toTwelveHour() - 1
    }
    val minuteIndex = remember(selectedTime) {
        selectedTime.minute
    }
    val meridiemIndex = remember(selectedTime) {
        if (selectedTime.hour < 12) 0 else 1
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TnyxTheme.dimens.CupertinoPickerHeight)
            .clipToBounds(),
        contentAlignment = Alignment.Center
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
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.86f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SleepCupertinoWheelColumn(
                    items = hours,
                    selectedIndex = hourIndex,
                    pickerHeight = pickerHeight,
                    itemHeight = itemHeight,
                    wrapAround = true,
                    itemStartPadding = TnyxTheme.dimens.SpaceXXS,
                    itemEndPadding = TnyxTheme.dimens.SpaceNone,
                    modifier = Modifier.weight(SLEEP_TIME_COLUMN_WEIGHT),
                    selectionShape = RoundedCornerShape(
                        topStart = TnyxTheme.dimens.RadiusM,
                        bottomStart = TnyxTheme.dimens.RadiusM,
                        topEnd = TnyxTheme.dimens.SpaceNone,
                        bottomEnd = TnyxTheme.dimens.SpaceNone,
                    ),
                    onItemSelected = { index, virtualIndex ->
                        if (virtualIndex == hourVirtualIndex) return@SleepCupertinoWheelColumn
                        val direction = if (virtualIndex > hourVirtualIndex) 1 else -1
                        selectedTime = sleepAdvanceToHour(
                            current = selectedTime,
                            targetHour12 = hours[index].toInt(),
                            direction = direction,
                        )
                        hourVirtualIndex = virtualIndex
                    }
                )
                Box(
                    modifier = Modifier
                        .width(TnyxTheme.dimens.SpaceS)
                        .height(itemHeight)
                        .background(TnyxTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = ":",
                        style = TnyxTheme.typography.titleLarge.copy(fontSize = SLEEP_WHEEL_SEPARATOR_TEXT_SIZE.sp),
                        color = TnyxTheme.colors.textPrimary,
                        fontWeight = FontWeight.Medium,
                    )
                }
                SleepCupertinoWheelColumn(
                    items = minutes,
                    selectedIndex = minuteIndex,
                    pickerHeight = pickerHeight,
                    itemHeight = itemHeight,
                    wrapAround = true,
                    itemStartPadding = TnyxTheme.dimens.SpaceNone,
                    itemEndPadding = TnyxTheme.dimens.SpaceXXS,
                    modifier = Modifier.weight(SLEEP_TIME_COLUMN_WEIGHT),
                    selectionShape = RoundedCornerShape(
                        topStart = TnyxTheme.dimens.SpaceNone,
                        bottomStart = TnyxTheme.dimens.SpaceNone,
                        topEnd = TnyxTheme.dimens.RadiusM,
                        bottomEnd = TnyxTheme.dimens.RadiusM,
                    ),
                    onItemSelected = { index, virtualIndex ->
                        if (virtualIndex == minuteVirtualIndex) return@SleepCupertinoWheelColumn
                        val direction = if (virtualIndex > minuteVirtualIndex) 1 else -1
                        selectedTime = sleepAdvanceToMinute(
                            current = selectedTime,
                            targetMinute = index,
                            direction = direction,
                        )
                        minuteVirtualIndex = virtualIndex
                    }
                )
                Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceS))
                SleepCupertinoMeridiemColumn(
                    items = meridiems,
                    selectedIndex = meridiemIndex,
                    pickerHeight = pickerHeight,
                    itemHeight = itemHeight,
                    itemStartPadding = TnyxTheme.dimens.SpaceXS,
                    modifier = Modifier.weight(SLEEP_MERIDIEM_COLUMN_WEIGHT),
                    selectionShape = RoundedCornerShape(TnyxTheme.dimens.RadiusM),
                    onItemSelected = { index ->
                        val selectedHour = selectedTime.hour.toTwelveHour()
                        val hour24 = when {
                            index == 0 && selectedHour == 12 -> 0
                            index == 0 -> selectedHour
                            selectedHour == 12 -> 12
                            else -> selectedHour + 12
                        }
                        selectedTime = selectedTime.withHour(hour24)
                    }
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

@Composable
private fun SleepCupertinoMeridiemColumn(
    items: List<String>,
    selectedIndex: Int,
    pickerHeight: Dp,
    itemHeight: Dp,
    onItemSelected: (Int) -> Unit,
    itemStartPadding: Dp = TnyxTheme.dimens.SpaceNone,
    itemEndPadding: Dp = TnyxTheme.dimens.SpaceNone,
    modifier: Modifier = Modifier,
    selectionShape: Shape = RoundedCornerShape(TnyxTheme.dimens.RadiusM),
) {
    val boundedSelectedIndex = selectedIndex.coerceIn(0, items.lastIndex)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = boundedSelectedIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val haptic = LocalHapticFeedback.current
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
    var lastHapticIndex by remember { mutableIntStateOf(boundedSelectedIndex) }

    LaunchedEffect(boundedSelectedIndex) {
        if (listState.firstVisibleItemIndex != boundedSelectedIndex) {
            listState.scrollToItem(boundedSelectedIndex)
        }
    }
    LaunchedEffect(centreIndex) {
        if (centreIndex != boundedSelectedIndex) {
            onItemSelected(centreIndex)
        }
        if (centreIndex != lastHapticIndex) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastHapticIndex = centreIndex
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
                                    (SLEEP_CUPERTINO_DIAMETER_RATIO * 2f)
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
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight)
                                .background(
                                    color = TnyxTheme.colors.surfaceVariant,
                                    shape = selectionShape,
                                )
                        )
                    }
                    Text(
                        text = item,
                        style = TnyxTheme.typography.titleLarge.copy(fontSize = SLEEP_WHEEL_TEXT_SIZE.sp),
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

@Composable
private fun SleepCupertinoWheelColumn(
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
    selectionShape: Shape = RoundedCornerShape(TnyxTheme.dimens.RadiusM),
) {
    val initialSelectedIndex = if (wrapAround) {
        sleepWrappedMiddleIndex(items.size, selectedIndex)
    } else {
        selectedIndex
    }
    val initialFirstVisibleIndex = (initialSelectedIndex - 3).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialFirstVisibleIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val haptic = LocalHapticFeedback.current
    val totalItemsCount = if (wrapAround) items.size * SLEEP_WRAP_REPEAT_COUNT else items.size
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
                centreVirtualIndex.sleepWrapIndex(items.size)
            } else {
                centreVirtualIndex.coerceIn(0, items.lastIndex)
            }
        }
    }
    var lastHapticIndex by remember { mutableIntStateOf(initialSelectedIndex) }

    LaunchedEffect(centreActualIndex, centreVirtualIndex) {
        onItemSelected(centreActualIndex, centreVirtualIndex)
        if (centreVirtualIndex != lastHapticIndex) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastHapticIndex = centreVirtualIndex
        }
    }
    LaunchedEffect(centreVirtualIndex, wrapAround, totalItemsCount) {
        if (!wrapAround || items.size <= 1) return@LaunchedEffect
        val safetyCycles = 20
        val lowerBound = items.size * safetyCycles
        val upperBound = totalItemsCount - lowerBound
        if (centreVirtualIndex in lowerBound..upperBound) return@LaunchedEffect
        val rebasedIndex = sleepWrappedMiddleIndex(items.size, centreActualIndex)
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
                    virtualIndex.sleepWrapIndex(items.size)
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
                                    (SLEEP_CUPERTINO_DIAMETER_RATIO * 2f)
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
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight)
                                .background(
                                    color = TnyxTheme.colors.surfaceVariant,
                                    shape = selectionShape,
                                )
                        )
                    }
                    Text(
                        text = item,
                        style = TnyxTheme.typography.titleLarge.copy(fontSize = SLEEP_WHEEL_TEXT_SIZE.sp),
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

private fun sleepWrappedMiddleIndex(size: Int, selectedIndex: Int): Int {
    if (size <= 0) return 0
    val middleCycle = SLEEP_WRAP_REPEAT_COUNT / 2
    return (middleCycle * size) + selectedIndex.sleepWrapIndex(size)
}

private fun Int.sleepWrapIndex(size: Int): Int {
    if (size <= 0) return 0
    return ((this % size) + size) % size
}

private fun sleepAdvanceToHour(
    current: LocalTime,
    targetHour12: Int,
    direction: Int,
): LocalTime {
    var candidate = current
    repeat(12) {
        if (candidate.hour.toTwelveHour() == targetHour12) {
            return candidate
        }
        candidate = if (direction >= 0) candidate.plusHours(1) else candidate.minusHours(1)
    }
    return candidate
}

private fun sleepAdvanceToMinute(
    current: LocalTime,
    targetMinute: Int,
    direction: Int,
): LocalTime {
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

private fun parseTime(timeStr: String): LocalTime? {
    return runCatching {
        val clean = timeStr.trim().uppercase()
        val formatters = listOf(
            DateTimeFormatter.ofPattern("h:mm a", Locale.US),
            DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
            DateTimeFormatter.ofPattern("HH:mm", Locale.US),
            DateTimeFormatter.ofPattern("H:mm", Locale.US)
        )
        for (formatter in formatters) {
            val parsed = runCatching { LocalTime.parse(clean, formatter) }.getOrNull()
            if (parsed != null) return@runCatching parsed
        }
        null
    }.getOrNull()
}

private fun formatTimeOfDay(time: LocalTime): String {
    return time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
}
