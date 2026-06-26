package com.tnyx.core.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Tnyx Weekly Calendar Component.
 * Caller owns selected date and domain-level date validation.
 */
@Composable
fun TnyxWeeklyCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    allowFutureDates: Boolean = false,
    today: LocalDate = LocalDate.now(),
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    locale: Locale = Locale.getDefault()
) {
    val weekDays = remember(selectedDate, firstDayOfWeek) {
        val daysFromStart = (selectedDate.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
        val startOfWeek = selectedDate.minusDays(daysFromStart.toLong())
        (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TnyxTheme.colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = TnyxTheme.dimens.SpaceS),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            weekDays.forEach { date ->
                val isSelected = date.isEqual(selectedDate)
                val isToday = date.isEqual(today)
                val isFuture = !allowFutureDates && date.isAfter(today)

                CalendarDayItem(
                    date = date,
                    isSelected = isSelected,
                    isToday = isToday,
                    isFuture = isFuture,
                    locale = locale,
                    onDateSelected = { if (!isFuture) onDateSelected(date) }
                )
            }
        }
        HorizontalDivider(
            color = TnyxTheme.colors.textPrimary.copy(alpha = 0.08f),
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
    locale: Locale,
    onDateSelected: () -> Unit
) {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
    val stateDescription = buildString {
        append(dayName)
        append(' ')
        append(date.dayOfMonth)
        if (isToday) append(", today")
        if (isSelected) append(", selected")
        if (isFuture) append(", unavailable")
    }

    Column(
        modifier = Modifier
            .width(TnyxTheme.dimens.CalendarDayWidth)
            .alpha(if (isFuture) 0.3f else 1f)
            .semantics {
                contentDescription = stateDescription
                selected = isSelected
                if (isFuture) disabled()
            }
            .clickable(enabled = !isFuture, role = Role.Button) { onDateSelected() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = TnyxTheme.typography.titleMedium,
            color = if (isSelected) TnyxTheme.colors.textPrimary else TnyxTheme.colors.textSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = dayName,
            style = TnyxTheme.typography.labelSmall,
            color = if (isSelected) TnyxTheme.colors.textPrimary else TnyxTheme.colors.textMuted
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXS))
        Box(
            modifier = Modifier
                .size(TnyxTheme.dimens.CalendarIndicatorSize)
                .background(
                    color = if (isToday) TnyxTheme.colors.textPrimary else Color.Transparent,
                    shape = TnyxTheme.shapes.Circle
                )
        )
    }
}
