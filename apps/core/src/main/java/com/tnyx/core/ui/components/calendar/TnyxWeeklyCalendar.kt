package com.tnyx.core.ui.components.calendar

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.*

private const val INITIAL_PAGE = 10000

/**
 * Tnyx Advanced Weekly Calendar.
 * Supports horizontal swiping, month jump-to-today, and Sunday highlighting.
 */
@Composable
fun TnyxWeeklyCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    allowFutureDates: Boolean = false,
    today: LocalDate = LocalDate.now(),
    locale: Locale = Locale.getDefault()
) {
    val tokens = TnyxTheme.components.calendar
    
    // Base date to calculate offsets (Monday of the current week)
    val baseDate = remember { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    
    // Calculate initial page based on selectedDate
    val initialPageOffset = ChronoUnit.WEEKS.between(baseDate, selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))).toInt()
    
    val pagerState = rememberPagerState(
        initialPage = INITIAL_PAGE + initialPageOffset,
        pageCount = { Int.MAX_VALUE } // Pseudo-infinite scrolling
    )

    // Sync pager when selectedDate changes from outside
    LaunchedEffect(selectedDate) {
        val targetWeekStart = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekDiff = ChronoUnit.WEEKS.between(baseDate, targetWeekStart).toInt()
        val targetPage = INITIAL_PAGE + weekDiff
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Determine which month to display on the left based on the visible page
    val displayedMonth = remember(pagerState.currentPage) {
        val weekOffset = pagerState.currentPage - INITIAL_PAGE
        val weekStart = baseDate.plusWeeks(weekOffset.toLong())
        weekStart.format(DateTimeFormatter.ofPattern("MMM", locale)).uppercase()
    }

    // Determine if the current week being viewed contains "Today"
    val isViewingCurrentWeek = remember(pagerState.currentPage) {
        val weekOffset = pagerState.currentPage - INITIAL_PAGE
        val weekStart = baseDate.plusWeeks(weekOffset.toLong())
        val weekEnd = weekStart.plusDays(6)
        !today.isBefore(weekStart) && !today.isAfter(weekEnd)
    }

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
            // --- Left Section: Month + Calendar Icon (Jump to Today) ---
            val sideContentColor = if (isViewingCurrentWeek) {
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
                        onClick = { onDateSelected(today) }
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
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXS))
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

                        CalendarDayItem(
                            date = date,
                            isSelected = isSelected,
                            isToday = isToday,
                            isFuture = isFuture,
                            isSunday = isSunday,
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
    onDateSelected: () -> Unit
) {
    val tokens = TnyxTheme.components.calendar
    
    val dayName = date.dayOfWeek.name.take(3).lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    // Color logic based on Sunday and Selection
    val primaryColor = when {
        isSunday && isSelected -> TnyxTheme.colors.error
        isSunday -> TnyxTheme.colors.error.copy(alpha = tokens.contentAlpha)
        isSelected -> TnyxTheme.colors.textPrimary
        else -> TnyxTheme.colors.textSecondary.copy(alpha = tokens.contentAlpha)
    }

    val labelColor = when {
        isSunday && isSelected -> TnyxTheme.colors.error
        isSunday -> TnyxTheme.colors.error.copy(alpha = tokens.labelAlpha)
        isSelected -> TnyxTheme.colors.textPrimary
        else -> TnyxTheme.colors.textSecondary.copy(alpha = tokens.labelAlpha)
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
        Text(
            text = date.dayOfMonth.toString(),
            style = TnyxTheme.typography.titleMedium,
            color = primaryColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXXS))
        Text(
            text = dayName,
            style = TnyxTheme.typography.labelSmall,
            color = labelColor,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXS))
        // Today Indicator
        Box(
            modifier = Modifier
                .size(tokens.indicatorSize)
                .background(
                    color = if (isToday) TnyxTheme.colors.textPrimary else Color.Transparent,
                    shape = TnyxTheme.shapes.Circle
                )
        )
    }
}
