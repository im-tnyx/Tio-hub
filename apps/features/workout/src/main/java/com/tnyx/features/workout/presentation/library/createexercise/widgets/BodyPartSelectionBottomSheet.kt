package com.tnyx.features.workout.presentation.library.createexercise.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet
import com.tnyx.features.workout.presentation.components.bodypart.BodyPartIconKey
import com.tnyx.features.workout.presentation.components.bodypart.TioBodyPartIcon

/**
 * Bottom sheet for selecting body parts out of 14 options matching [BodyPartIconKey].
 * Supports multi-selection mode with radio button indicators.
 */
@Composable
fun BodyPartSelectionBottomSheet(
    visible: Boolean,
    selectedBodyPart: String,
    onBodyPartSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    isMultiSelect: Boolean = true,
) {
    val bodyPartOptions = remember { BodyPartIconKey.entries }

    var selectedSet by remember(selectedBodyPart, visible) {
        val initial = selectedBodyPart
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.contains("optional", ignoreCase = true) && !it.equals("Select", ignoreCase = true) }
            .toSet()
        mutableStateOf(initial)
    }

    TnyxModalBottomSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = "Select Body Part",
        contentHorizontalPadding = 0.dp,
        contentBottomPadding = 0.dp,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
        ) {
            items(bodyPartOptions, key = { it.name }) { bodyPart ->
                val formattedName = remember(bodyPart.name) {
                    bodyPart.name.lowercase().replaceFirstChar { it.uppercase() }
                }
                val isSelected = if (isMultiSelect) {
                    selectedSet.any { it.equals(formattedName, ignoreCase = true) || it.equals(bodyPart.name, ignoreCase = true) }
                } else {
                    selectedBodyPart.equals(formattedName, ignoreCase = true) || selectedBodyPart.equals(bodyPart.name, ignoreCase = true)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable {
                            if (isMultiSelect) {
                                val newSet = if (isSelected) {
                                    selectedSet.filter { !it.equals(formattedName, ignoreCase = true) && !it.equals(bodyPart.name, ignoreCase = true) }.toSet()
                                } else {
                                    selectedSet + formattedName
                                }
                                selectedSet = newSet
                                val result = if (newSet.isEmpty()) "Select (optional)" else newSet.joinToString(", ")
                                onBodyPartSelected(result)
                            } else {
                                onBodyPartSelected(formattedName)
                            }
                        }
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
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // ── Body Part Icon (48dp x 48dp, solid white bg) ──
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                TioBodyPartIcon(
                                    bodyPart = bodyPart.name,
                                    contentDescription = formattedName,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = formattedName,
                                style = TnyxTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (isSelected) TnyxTheme.colors.accent else TnyxTheme.colors.textPrimary,
                            )
                        }

                        // ── Checkbox Indicator (Square with Checkmark for Multi-Selection) ──
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = TnyxTheme.colors.accent,
                                uncheckedColor = TnyxTheme.colors.textSecondary.copy(alpha = 0.4f),
                                checkmarkColor = Color.White,
                            )
                        )
                    }

                    // ── Inset Divider (1dp, alpha=0.12) ──
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(1.dp)
                            .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.12f))
                    )
                }
            }
        }
    }
}
