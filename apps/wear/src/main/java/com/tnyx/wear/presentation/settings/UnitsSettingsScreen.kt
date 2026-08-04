package com.tnyx.wear.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Dialog
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.tnyx.wear.theme.BackgroundBlack
import com.tnyx.wear.theme.CardBackground
import com.tnyx.wear.theme.ColorSteps
import com.tnyx.wear.theme.TextGray
import com.tnyx.wear.theme.TextWhite
import com.tnyx.wear.theme.WearTypography

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun UnitsSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val columnState = rememberColumnState()

    // Dialog States
    var showEnergyDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showDistanceDialog by remember { mutableStateOf(false) }

    // Active Preferences
    var selectedEnergyUnit by remember { mutableStateOf("kcal") }
    var selectedWeightUnit by remember { mutableStateOf("kg") }
    var selectedDistanceUnit by remember { mutableStateOf("km") }

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundBlack)
        ) {
            // Header
            item {
                Text(
                    text = "Units Settings",
                    color = TextWhite,
                    style = WearTypography.title1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 1. Energy Unit
            item {
                Chip(
                    onClick = { showEnergyDialog = true },
                    label = { Text("Energy Unit", color = TextWhite) },
                    secondaryLabel = { Text(selectedEnergyUnit, color = TextGray) },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = CardBackground,
                        contentColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2. Weight Unit
            item {
                Chip(
                    onClick = { showWeightDialog = true },
                    label = { Text("Weight Unit", color = TextWhite) },
                    secondaryLabel = { Text(selectedWeightUnit, color = TextGray) },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = CardBackground,
                        contentColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Distance Unit
            item {
                Chip(
                    onClick = { showDistanceDialog = true },
                    label = { Text("Distance Unit", color = TextWhite) },
                    secondaryLabel = { Text(selectedDistanceUnit, color = TextGray) },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = CardBackground,
                        contentColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // --- SELECTION DIALOGS ---

    // A. Energy Unit Dialog
    Dialog(
        showDialog = showEnergyDialog,
        onDismissRequest = { showEnergyDialog = false }
    ) {
        val dialogColumnState = rememberColumnState()
        ScreenScaffold(scrollState = dialogColumnState) {
            ScalingLazyColumn(
                columnState = dialogColumnState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlack)
            ) {
                item {
                    Text(
                        text = "Energy Unit",
                        color = TextWhite,
                        style = WearTypography.title1,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item {
                    Chip(
                        onClick = {
                            selectedEnergyUnit = "kcal"
                            showEnergyDialog = false
                        },
                        label = { Text("Calories (kcal)", color = if (selectedEnergyUnit == "kcal") BackgroundBlack else TextWhite) },
                        colors = if (selectedEnergyUnit == "kcal") {
                            ChipDefaults.primaryChipColors(backgroundColor = ColorSteps)
                        } else {
                            ChipDefaults.secondaryChipColors(backgroundColor = CardBackground)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Chip(
                        onClick = {
                            selectedEnergyUnit = "kJ"
                            showEnergyDialog = false
                        },
                        label = { Text("Kilojoules (kJ)", color = if (selectedEnergyUnit == "kJ") BackgroundBlack else TextWhite) },
                        colors = if (selectedEnergyUnit == "kJ") {
                            ChipDefaults.primaryChipColors(backgroundColor = ColorSteps)
                        } else {
                            ChipDefaults.secondaryChipColors(backgroundColor = CardBackground)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // B. Weight Unit Dialog
    Dialog(
        showDialog = showWeightDialog,
        onDismissRequest = { showWeightDialog = false }
    ) {
        val dialogColumnState = rememberColumnState()
        ScreenScaffold(scrollState = dialogColumnState) {
            ScalingLazyColumn(
                columnState = dialogColumnState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlack)
            ) {
                item {
                    Text(
                        text = "Weight Unit",
                        color = TextWhite,
                        style = WearTypography.title1,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item {
                    Chip(
                        onClick = {
                            selectedWeightUnit = "kg"
                            showWeightDialog = false
                        },
                        label = { Text("Kilograms (kg)", color = if (selectedWeightUnit == "kg") BackgroundBlack else TextWhite) },
                        colors = if (selectedWeightUnit == "kg") {
                            ChipDefaults.primaryChipColors(backgroundColor = ColorSteps)
                        } else {
                            ChipDefaults.secondaryChipColors(backgroundColor = CardBackground)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Chip(
                        onClick = {
                            selectedWeightUnit = "lbs"
                            showWeightDialog = false
                        },
                        label = { Text("Pounds (lbs)", color = if (selectedWeightUnit == "lbs") BackgroundBlack else TextWhite) },
                        colors = if (selectedWeightUnit == "lbs") {
                            ChipDefaults.primaryChipColors(backgroundColor = ColorSteps)
                        } else {
                            ChipDefaults.secondaryChipColors(backgroundColor = CardBackground)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // C. Distance Unit Dialog
    Dialog(
        showDialog = showDistanceDialog,
        onDismissRequest = { showDistanceDialog = false }
    ) {
        val dialogColumnState = rememberColumnState()
        ScreenScaffold(scrollState = dialogColumnState) {
            ScalingLazyColumn(
                columnState = dialogColumnState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlack)
            ) {
                item {
                    Text(
                        text = "Distance Unit",
                        color = TextWhite,
                        style = WearTypography.title1,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item {
                    Chip(
                        onClick = {
                            selectedDistanceUnit = "km"
                            showDistanceDialog = false
                        },
                        label = { Text("Kilometers (km)", color = if (selectedDistanceUnit == "km") BackgroundBlack else TextWhite) },
                        colors = if (selectedDistanceUnit == "km") {
                            ChipDefaults.primaryChipColors(backgroundColor = ColorSteps)
                        } else {
                            ChipDefaults.secondaryChipColors(backgroundColor = CardBackground)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Chip(
                        onClick = {
                            selectedDistanceUnit = "mi"
                            showDistanceDialog = false
                        },
                        label = { Text("Miles (mi)", color = if (selectedDistanceUnit == "mi") BackgroundBlack else TextWhite) },
                        colors = if (selectedDistanceUnit == "mi") {
                            ChipDefaults.primaryChipColors(backgroundColor = ColorSteps)
                        } else {
                            ChipDefaults.secondaryChipColors(backgroundColor = CardBackground)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
