package com.tnyx.features.workout.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.components.TnyxHeaderSize
import com.tnyx.core.ui.components.calendar.TnyxWeeklyCalendar
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader
import com.tnyx.features.workout.R
import java.time.LocalDate

@Composable
fun WorkoutScreen(
    state: WorkoutUiState,
    onAction: (WorkoutAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Local date state — no scroll, header + calendar both static
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding()
    ) {
        // Static TopBar — does not scroll
        TnyxScreenHeader(
            title = "Workout",
            size = TnyxHeaderSize.Compact,
            uppercaseTitle = false,
            actions = {
                IconButton(onClick = { /* TODO: open calendar/date picker */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = "Calendar",
                        tint = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { onAction(WorkoutAction.HistoryClicked) }) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "Workout History",
                        tint = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        // Static Weekly Calendar — does not scroll
        TnyxWeeklyCalendar(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        // Workout content container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TnyxTheme.colors.background)
        )
    }
}



@Composable
fun WorkoutHistoryScreen(
    state: WorkoutUiState,
    onAction: (WorkoutAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background),
    )
}
