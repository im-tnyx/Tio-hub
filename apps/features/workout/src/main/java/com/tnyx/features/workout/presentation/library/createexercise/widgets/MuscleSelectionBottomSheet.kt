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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet
import com.tnyx.features.workout.presentation.components.musclemap.DetailedMuscleItem
import com.tnyx.features.workout.presentation.components.musclemap.DetailedMuscleList
import com.tnyx.features.workout.presentation.components.musclemap.MuscleMapView
import com.tnyx.features.workout.presentation.components.musclemap.TioMuscleMap
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant

/**
 * Full-screen muscle selection bottom sheet matching Lyfta 1.581 parity.
 *
 * Layout reference: `muscle_item_layout.xml` + `bottom_sheet_list_layout.xml`
 * - Item row height: 72dp
 * - Avatar circle: 58dp, start margin 16dp
 * - Text: 14sp Medium, 16dp from avatar end
 * - Radio/Checkbox end: 16dp from parent end
 * - Divider: 1dp, alpha=0.12, marginStart=16dp, marginEnd=16dp, pinned to bottom of each row
 * - Header divider: 1dp, alpha=0.12, full width, below sheet title
 * - No rounded Surface card per item (Lyfta uses flat selectableItemBackground)
 * - Selected state: full-row background tint colorPrimary alpha=0.12
 */
@Composable
fun MuscleSelectionBottomSheet(
    visible: Boolean,
    title: String,
    selectedMuscle: String,
    onMuscleSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ExerciseMediaVariant = ExerciseMediaVariant.MALE,
    isMultiSelect: Boolean = false,
    isSecondarySheet: Boolean = false,
    /** When non-null, filters the muscle list to only show muscles for this body part regionKey. */
    bodyPartFilter: String? = null,
) {
    TnyxModalBottomSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = title,
        contentHorizontalPadding = 0.dp,
        contentBottomPadding = 0.dp,
        modifier = modifier,
    ) {
        val muscles = remember(bodyPartFilter) {
            if (bodyPartFilter.isNullOrBlank()) {
                DetailedMuscleList
            } else {
                val filterSet = bodyPartFilter.split(",")
                    .map { it.trim().lowercase() }
                    .filter { it.isNotEmpty() }
                    .toSet()
                if (filterSet.isEmpty()) DetailedMuscleList
                else DetailedMuscleList.filter { it.regionKey in filterSet }
            }
        }

        var selectedSet by remember(selectedMuscle, visible) {
            val initial = selectedMuscle
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.contains("optional", ignoreCase = true) && !it.equals("Select", ignoreCase = true) }
                .toSet()
            mutableStateOf(initial)
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 680.dp)
            ) {
                items(muscles, key = { it.id }) { item ->
                    val isSelected = if (isMultiSelect) {
                        selectedSet.contains(item.displayName) || selectedSet.contains(item.id)
                    } else {
                        selectedMuscle.equals(item.displayName, ignoreCase = true) ||
                                selectedMuscle.equals(item.id, ignoreCase = true)
                    }

                    MuscleItemRow(
                        item = item,
                        isSelected = isSelected,
                        isMultiSelect = isMultiSelect,
                        isSecondarySheet = isSecondarySheet,
                        variant = variant,
                        onToggle = {
                            if (isMultiSelect) {
                                val newSet = if (isSelected) {
                                    selectedSet - item.displayName
                                } else {
                                    selectedSet + item.displayName
                                }
                                selectedSet = newSet
                                val result = if (newSet.isEmpty()) "Select (optional)" else newSet.joinToString(", ")
                                onMuscleSelected(result)
                            } else {
                                onMuscleSelected(item.displayName)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Single muscle row — matches `muscle_item_layout.xml` exactly:
 * - Height: 72dp
 * - Start padding: 16dp (avatar), Text 16dp from avatar end, Checkbox 16dp from end
 * - Selected highlight: full-row overlay, colorPrimary alpha=0.12 (no card/rounded shape)
 * - Bottom divider: 1dp, alpha=0.12, inset 16dp start+end (pinned at bottom of row)
 */
@Composable
private fun MuscleItemRow(
    item: DetailedMuscleItem,
    isSelected: Boolean,
    isMultiSelect: Boolean,
    isSecondarySheet: Boolean,
    variant: ExerciseMediaVariant,
    onToggle: () -> Unit,
) {
    val alignment = when (item.regionKey) {
        "neck", "chest", "shoulders", "biceps", "triceps", "forearms", "back" -> Alignment.TopCenter
        "quadriceps", "calves", "hamstrings" -> Alignment.BottomCenter
        else -> Alignment.Center
    }

    val activeColor = if (isSecondarySheet) TnyxTheme.colors.secondaryMuscle else TnyxTheme.colors.accent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onToggle)
    ) {
        // ── Selected state background overlay (alpha=0.12, full row) ──
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(activeColor.copy(alpha = 0.12f))
            )
        }

        // ── Row content ──
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // ── Avatar circle (48dp, white bg, matching Body Part & Equipment parity) ──
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    TioMuscleMap(
                        muscleGroups = if (isSecondarySheet) emptyList() else listOf(item.id),
                        secondaryMuscles = if (isSecondarySheet) listOf(item.id) else emptyList(),
                        variant = variant,
                        view = item.defaultView,
                        contentDescription = item.displayName,
                        contentScale = ContentScale.Crop,
                        alignment = alignment,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // ── Muscle name text (14sp Medium) ──
                Text(
                    text = item.displayName,
                    style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = if (isSelected) activeColor else TnyxTheme.colors.textPrimary,
                )
            }

            // ── Checkbox Indicator (Square with Checkmark for Multi-Selection) ──
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = activeColor,
                    uncheckedColor = TnyxTheme.colors.textSecondary.copy(alpha = 0.4f),
                    checkmarkColor = Color.White,
                )
            )
        }

        // ── Bottom divider (1dp, alpha=0.12, inset 16dp, pinned at bottom) ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
                .height(1.dp)
                .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.12f))
        )
    }
}
