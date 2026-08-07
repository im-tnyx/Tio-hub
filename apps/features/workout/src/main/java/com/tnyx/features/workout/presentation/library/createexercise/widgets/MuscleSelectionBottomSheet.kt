package com.tnyx.features.workout.presentation.library.createexercise.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.tnyx.features.workout.presentation.components.musclemap.MuscleMapRegionKey
import com.tnyx.features.workout.presentation.components.musclemap.MuscleMapView
import com.tnyx.features.workout.presentation.components.musclemap.TioMuscleMap
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant

@Composable
fun MuscleSelectionBottomSheet(
    visible: Boolean,
    title: String,
    selectedMuscle: String,
    onMuscleSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TnyxModalBottomSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = title,
        modifier = modifier,
    ) {
        val muscles = remember { MuscleMapRegionKey.entries }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
        ) {
            items(muscles, key = { it.name }) { region ->
                val formattedName = remember(region) {
                    region.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
                }
                val isSelected = selectedMuscle.equals(formattedName, ignoreCase = true) || selectedMuscle.equals(region.name, ignoreCase = true)
                val defaultView = when (region) {
                    MuscleMapRegionKey.BACK,
                    MuscleMapRegionKey.TRICEPS,
                    MuscleMapRegionKey.HAMSTRINGS,
                    MuscleMapRegionKey.CALVES,
                    MuscleMapRegionKey.HIPS -> MuscleMapView.BACK
                    else -> MuscleMapView.FRONT
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMuscleSelected(formattedName) }
                        .padding(vertical = TnyxDimens.SpaceS, horizontal = TnyxDimens.SpaceXXS),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(TnyxDimens.RadiusS),
                            color = TnyxTheme.colors.surfaceVariant,
                            border = BorderStroke(TnyxDimens.BorderThin, TnyxTheme.colors.surfaceVariant),
                            modifier = Modifier.size(56.dp)
                        ) {
                            TioMuscleMap(
                                muscleGroups = listOf(region.name),
                                variant = ExerciseMediaVariant.MALE,
                                view = defaultView,
                                contentDescription = formattedName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(TnyxDimens.SpaceXXS)
                            )
                        }

                        Spacer(modifier = Modifier.width(TnyxDimens.SpaceM))

                        Text(
                            text = formattedName,
                            style = TnyxTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = if (isSelected) TnyxTheme.colors.accent else TnyxTheme.colors.textPrimary
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Selected",
                            tint = TnyxTheme.colors.accent,
                            modifier = Modifier.size(TnyxDimens.IconM)
                        )
                    }
                }

                HorizontalDivider(
                    color = TnyxTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                    thickness = TnyxDimens.BorderSubtle
                )
            }
        }
    }
}
