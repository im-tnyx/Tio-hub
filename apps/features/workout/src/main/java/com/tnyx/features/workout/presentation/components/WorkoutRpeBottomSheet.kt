package com.tnyx.features.workout.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.buttons.TnyxGhostButton
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet
import com.tnyx.features.workout.presentation.WorkoutRpePickerUi
import kotlin.math.roundToInt

@Composable
fun WorkoutRpeBottomSheet(
    state: WorkoutRpePickerUi?,
    onDismiss: () -> Unit,
    onSelected: (Int) -> Unit,
) {
    val picker = state ?: return
    var sliderValue by remember(picker.exerciseEntryId, picker.setId) {
        mutableFloatStateOf((picker.selectedRpe ?: DEFAULT_RPE).toFloat())
    }
    val selectedRpe = sliderValue.roundToInt().coerceIn(MIN_RPE, MAX_RPE)
    val description = selectedRpe.description()

    TnyxModalBottomSheet(
        visible = true,
        onDismissRequest = onDismiss,
        title = "Select RPE",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Set ${picker.setNumber}",
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary,
            )
            Text(
                text = selectedRpe.toString(),
                style = TnyxTheme.typography.displayMedium,
                color = TnyxTheme.colors.accent,
            )
            Text(
                text = description.first,
                style = TnyxTheme.typography.titleMedium,
                color = TnyxTheme.colors.textPrimary,
            )
            Text(
                text = description.second,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary,
            )
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it.roundToInt().toFloat() },
                valueRange = MIN_RPE.toFloat()..MAX_RPE.toFloat(),
                steps = RPE_STEPS,
                colors = SliderDefaults.colors(
                    thumbColor = TnyxTheme.colors.accent,
                    activeTrackColor = TnyxTheme.colors.accent,
                    inactiveTrackColor = TnyxTheme.colors.surfaceVariant,
                    activeTickColor = TnyxTheme.colors.onPrimary,
                    inactiveTickColor = TnyxTheme.colors.textMuted,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                (MIN_RPE..MAX_RPE).forEach { value ->
                    Text(
                        text = value.toString(),
                        style = TnyxTheme.typography.labelSmall,
                        color = TnyxTheme.colors.textMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            TnyxPrimaryButton(
                text = "Apply RPE $selectedRpe",
                onPressed = { onSelected(selectedRpe) },
                expand = true,
            )
            TnyxGhostButton(
                text = "Cancel",
                onPressed = onDismiss,
                expand = true,
            )
        }
    }
}

private fun Int.description(): Pair<String, String> = when (this) {
    5 -> "Very Easy" to "5+ reps left"
    6 -> "Easy" to "4 reps left"
    7 -> "Moderate" to "3 reps left"
    8 -> "Hard" to "2 reps left"
    9 -> "Very Hard" to "1 rep left"
    else -> "Max Effort" to "0 reps left"
}

private const val MIN_RPE = 5
private const val MAX_RPE = 10
private const val DEFAULT_RPE = 8
private const val RPE_STEPS = 4
