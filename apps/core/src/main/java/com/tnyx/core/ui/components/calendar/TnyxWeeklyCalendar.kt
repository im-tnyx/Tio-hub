package com.tnyx.core.ui.components.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxPalette
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.*

private const val INITIAL_PAGE = 10000

/**
 * Data decoration model for dynamic calendar day badges.
 */
@Immutable
data class CalendarDayDecoration(
    val hasPlan: Boolean = false,
    val scheduledCount: Int = 0,
    val progressFraction: Float? = null,
    val isCompleted: Boolean = false,
)

/**
 * Tnyx Advanced Weekly Calendar.
 * Supports configurable starting day of week (Default: Sunday), Day top / Date bottom layout,
 * 3-Tier Dynamic Rings, multi-schedule dots, and horizontal week swiping.
 */
@Composable
fun TnyxWeeklyCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    decorations: Map<LocalDate, CalendarDayDecoration> = emptyMap(),
    allowFutureDates: Boolean = false,
    today: LocalDate = LocalDate.now(),
    locale: Locale = Locale.getDefault()
) {
    val tokens = TnyxTheme.components.calendar

    // Base date to calculate offsets (Start of the current week based on firstDayOfWeek)
    val baseDate = remember(today, firstDayOfWeek) {
        today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    }

    // Calculate initial page based on selectedDate
    val initialPageOffset = ChronoUnit.WEEKS.between(
        baseDate,
        selectedDate.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    ).toInt()

    // Determine maximum page limit based on allowFutureDates
    val maxPage = remember(allowFutureDates, baseDate, today, firstDayOfWeek) {
        if (allowFutureDates) {
            Int.MAX_VALUE
        } else {
            val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
            val currentWeekOffset = ChronoUnit.WEEKS.between(baseDate, currentWeekStart).toInt()
            INITIAL_PAGE + currentWeekOffset + 1
        }
    }

    val pagerState = rememberPagerState(
        initialPage = INITIAL_PAGE + initialPageOffset,
        pageCount = { maxPage }
    )

    // Sync pager when selectedDate changes from outside
    LaunchedEffect(selectedDate, firstDayOfWeek) {
        val targetWeekStart = selectedDate.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
        val weekDiff = ChronoUnit.WEEKS.between(baseDate, targetWeekStart).toInt()
        val targetPage = INITIAL_PAGE + weekDiff
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Determine which month to display on the left based on the visible page
    val displayedMonth = remember(pagerState.currentPage, baseDate) {
        val weekOffset = pagerState.currentPage - INITIAL_PAGE
        val weekStart = baseDate.plusWeeks(weekOffset.toLong())
        weekStart.format(DateTimeFormatter.ofPattern("MMM", locale)).uppercase()
    }

    // Determine if the current week being viewed contains "Today"
    val isViewingCurrentWeek = remember(pagerState.currentPage, baseDate, today) {
        val weekOffset = pagerState.currentPage - INITIAL_PAGE
        val weekStart = baseDate.plusWeeks(weekOffset.toLong())
        val weekEnd = weekStart.plusDays(6)
        !today.isBefore(weekStart) && !today.isAfter(weekEnd)
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TnyxTheme.colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.height),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Left Section: Month + Calendar Icon (Jump to Today & Current Week) ---
            val isTodaySelectedAndVisible = isViewingCurrentWeek && selectedDate.isEqual(today)
            val sideContentColor = if (isTodaySelectedAndVisible) {
                TnyxTheme.colors.textSecondary.copy(alpha = tokens.contentAlpha)
            } else {
                TnyxTheme.colors.accent
            }

            Column(
                modifier = Modifier
                    .width(tokens.sideSectionWidth)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onDateSelected(today)
                            coroutineScope.launch {
                                val targetWeekStart = today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                                val weekDiff = ChronoUnit.WEEKS.between(baseDate, targetWeekStart).toInt()
                                val targetPage = INITIAL_PAGE + weekDiff
                                if (pagerState.currentPage != targetPage) {
                                    pagerState.animateScrollToPage(targetPage)
                                }
                            }
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = "Today",
                    modifier = Modifier.size(tokens.iconSize),
                    tint = sideContentColor
                )
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXXS))
                Text(
                    text = displayedMonth,
                    style = TnyxTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = sideContentColor
                )
            }

            // --- Vertical Divider ---
            Box(
                modifier = Modifier
                    .width(TnyxTheme.dimens.BorderThin)
                    .fillMaxHeight(0.65f)
                    .background(TnyxTheme.colors.textPrimary.copy(alpha = tokens.dividerAlpha))
            )

            // --- Right Section: Swipable Weeks ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = TnyxTheme.dimens.SpaceXS)
            ) { page ->
                val weekOffset = page - INITIAL_PAGE
                val weekStart = baseDate.plusWeeks(weekOffset.toLong())

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (0..6).forEach { dayIndex ->
                        val date = weekStart.plusDays(dayIndex.toLong())
                        val isSelected = date.isEqual(selectedDate)
                        val isToday = date.isEqual(today)
                        val isFuture = !allowFutureDates && date.isAfter(today)
                        val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
                        val decoration = decorations[date] ?: CalendarDayDecoration()

                        CalendarDayItem(
                            date = date,
                            isSelected = isSelected,
                            isToday = isToday,
                            isFuture = isFuture,
                            isSunday = isSunday,
                            decoration = decoration,
                            onDateSelected = { if (!isFuture) onDateSelected(date) }
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            color = TnyxTheme.colors.textPrimary.copy(alpha = tokens.dividerAlpha),
            thickness = TnyxTheme.dimens.BorderThin
        )
    }
}

@Composable
private fun CalendarDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    isSunday: Boolean,
    decoration: CalendarDayDecoration,
    onDateSelected: () -> Unit
) {
    val tokens = TnyxTheme.components.calendar

    val dayName = date.dayOfWeek.name.take(3).lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    val accentColor = TnyxTheme.colors.accent
    val pumpkinColor = TnyxPalette.Pumpkin

    val isHighlight = isSelected || isToday

    // Color logic: Selected/Today uses full opacity bold primary text, unselected dates use subtle textSecondary
    val dateTextColor = when {
        decoration.isCompleted -> TnyxTheme.colors.onPrimary
        isSunday -> if (isHighlight) TnyxTheme.colors.error else TnyxTheme.colors.error.copy(alpha = 0.7f)
        isHighlight -> TnyxTheme.colors.textPrimary
        else -> TnyxTheme.colors.textSecondary.copy(alpha = 0.75f)
    }

    val dayNameColor = when {
        isSunday -> if (isHighlight) TnyxTheme.colors.error else TnyxTheme.colors.error.copy(alpha = 0.6f)
        isHighlight -> TnyxTheme.colors.textPrimary
        else -> TnyxTheme.colors.textSecondary.copy(alpha = 0.6f)
    }

    Column(
        modifier = Modifier
            .width(tokens.dayWidth)
            .alpha(if (isFuture) tokens.futureAlpha else 1f)
            .clickable(
                enabled = !isFuture,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDateSelected
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- 1. TOP ROW: Day Name (SUN, MON, TUE...) ---
        Text(
            text = dayName.uppercase(),
            style = TnyxTheme.typography.labelSmall,
            color = dayNameColor,
            fontSize = 11.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(2.dp))

        // --- 2. MIDDLE ROW: Date Number inside Dynamic Ring Badge ---
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Dynamic Canvas Rings & Badges (Selection draws no ring)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth05 = 0.5.dp.toPx()
                val strokeWidth10 = 1.0.dp.toPx()

                when {
                    // 1. Workout Completed: Filled accent circle badge
                    decoration.isCompleted -> {
                        drawCircle(
                            color = accentColor,
                            radius = size.minDimension / 2f
                        )
                    }
                    // 2. Meal Diary Progress Arc (1dp stroke, active when meals are logged)
                    decoration.progressFraction != null && decoration.progressFraction > 0f -> {
                        val progress = decoration.progressFraction.coerceIn(0f, 1f)
                        val radius10 = (size.minDimension - strokeWidth10) / 2f
                        // Subtle background track (1dp)
                        drawCircle(
                            color = accentColor.copy(alpha = 0.15f),
                            radius = radius10,
                            style = Stroke(width = strokeWidth10)
                        )
                        // Filled progress arc (1dp)
                        drawArc(
                            color = accentColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth10, cap = StrokeCap.Round)
                        )
                    }
                    // 3. Planned Target Outline Ring (0.5dp subtle outline)
                    decoration.hasPlan -> {
                        val radius05 = (size.minDimension - strokeWidth05) / 2f
                        drawCircle(
                            color = pumpkinColor,
                            radius = radius05,
                            style = Stroke(width = strokeWidth05)
                        )
                    }
                }
            }

            // Date Number Text
            Text(
                text = date.dayOfMonth.toString(),
                style = TnyxTheme.typography.titleMedium,
                color = dateTextColor,
                fontWeight = if (isHighlight || decoration.isCompleted) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // --- 3. BOTTOM ROW: Multi-schedule Dots / Today Indicator ---
        Row(
            modifier = Modifier.height(6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (decoration.scheduledCount > 0) {
                // Render 1 to 3 Pumpkin dots for scheduled workouts
                val dotsCount = decoration.scheduledCount.coerceIn(1, 3)
                repeat(dotsCount) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color = pumpkinColor,
                                shape = TnyxTheme.shapes.Circle
                            )
                    )
                }
            } else if (isToday) {
                // Today Indicator dot
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = if (isSelected) accentColor else TnyxTheme.colors.textPrimary,
                            shape = TnyxTheme.shapes.Circle
                        )
                )
            }
        }
    }
}
