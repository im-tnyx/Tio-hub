package com.tnyx.features.workout.presentation.library.createexercise.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxDimens
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet

data class ExerciseTypeOption(
    val id: String,
    val title: String,
    val example: String,
    val badges: List<String>,
)

val ExerciseTypeOptions = listOf(
    ExerciseTypeOption(
        id = "weight_reps",
        title = "Weight & Reps",
        example = "e.g. Bench press, Squat, Deadlift",
        badges = listOf("KG", "REPS")
    ),
    ExerciseTypeOption(
        id = "distance_duration",
        title = "Distance & Duration",
        example = "e.g. Running, Cycling, Swimming",
        badges = listOf("KM", "TIME")
    ),
    ExerciseTypeOption(
        id = "duration",
        title = "Duration",
        example = "e.g. Plank, Wall Sit",
        badges = listOf("TIME")
    ),
    ExerciseTypeOption(
        id = "db_2_simultaneous",
        title = "Dumbbell (Both Arms)",
        example = "e.g. Dumbbell bench press, Dumbbell fly",
        badges = listOf("KG", "REPS", "2DB")
    ),
    ExerciseTypeOption(
        id = "db_1_alt_sides",
        title = "Dumbbell (Alternating Arms)",
        example = "e.g. Dumbbell one-arm row, Alternating bicep curls",
        badges = listOf("KG", "REPS", "1DB")
    ),
    ExerciseTypeOption(
        id = "db_1_both_sides",
        title = "Dumbbell (Single)",
        example = "e.g. Pullover, Goblet squat",
        badges = listOf("KG", "REPS", "1DB")
    ),
    ExerciseTypeOption(
        id = "db_2_alt_legs",
        title = "Dumbbell (Double - Alternating Legs)",
        example = "e.g. Lunges, Bulgarian split squats (With 2 dumbbells)",
        badges = listOf("KG", "REPS", "2DB")
    ),
    ExerciseTypeOption(
        id = "db_1_alt_legs",
        title = "Dumbbell (Single - Alternating Legs)",
        example = "e.g. Lunges, Bulgarian split squats (With 1 dumbbell)",
        badges = listOf("KG", "REPS", "1DB")
    ),
    ExerciseTypeOption(
        id = "full_bodyweight",
        title = "Bodyweight Reps",
        example = "e.g. Pull-ups, Dips",
        badges = listOf("+KG", "REPS")
    ),
    ExerciseTypeOption(
        id = "bodyweight_assisted",
        title = "Assisted Bodyweight",
        example = "e.g. Assisted pull-up, Assisted dip",
        badges = listOf("-KG", "REPS")
    ),
    ExerciseTypeOption(
        id = "steps_duration",
        title = "Steps & Duration",
        example = "e.g. Step mill, Elliptical, Stair climbing",
        badges = listOf("STEPS", "TIME")
    ),
)

/**
 * Bottom sheet for selecting an exercise tracking type out of 11 options matching Lyfta parity.
 * Includes title, example exercises, tracking chip badges, and radio button indicators.
 */
@Composable
fun ExerciseTypeSelectionBottomSheet(
    visible: Boolean,
    selectedExerciseType: String,
    onExerciseTypeSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = remember { ExerciseTypeOptions }

    TnyxModalBottomSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = "Select Exercise Type",
        contentHorizontalPadding = 0.dp,
        contentBottomPadding = 0.dp,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
        ) {
            items(options, key = { it.id }) { option ->
                val isSelected = selectedExerciseType.equals(option.title, ignoreCase = true) ||
                        selectedExerciseType.equals(option.id, ignoreCase = true)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExerciseTypeSelected(option.title) }
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TnyxTheme.colors.accent.copy(alpha = 0.12f))
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(TnyxDimens.SpaceM),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.title,
                                style = TnyxTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (isSelected) TnyxTheme.colors.accent else TnyxTheme.colors.textPrimary,
                            )

                            Spacer(modifier = Modifier.height(TnyxDimens.SpaceXS))

                            Text(
                                text = option.example,
                                style = TnyxTheme.typography.bodySmall,
                                color = TnyxTheme.colors.textSecondary.copy(alpha = 0.7f),
                            )

                            Spacer(modifier = Modifier.height(TnyxDimens.SpaceS))

                            Row(horizontalArrangement = Arrangement.spacedBy(TnyxDimens.SpaceXS)) {
                                option.badges.forEach { badge ->
                                    Surface(
                                        shape = RoundedCornerShape(TnyxDimens.RadiusXS),
                                        color = TnyxTheme.colors.accent.copy(alpha = if (isSelected) 0.25f else 0.12f),
                                    ) {
                                        Text(
                                            text = badge,
                                            style = TnyxTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = TnyxTheme.colors.accent,
                                            modifier = Modifier.padding(horizontal = TnyxDimens.SpaceS, vertical = TnyxDimens.SpaceXXS)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(TnyxDimens.SpaceSM))

                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = TnyxTheme.colors.accent,
                                unselectedColor = TnyxTheme.colors.textSecondary.copy(alpha = 0.4f),
                            )
                        )
                    }

                    // ── Inset Divider (1dp, alpha=0.12) ──
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = TnyxDimens.SpaceM)
                            .height(TnyxDimens.BorderThin)
                            .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.12f))
                    )
                }
            }
        }
    }
}
