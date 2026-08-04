package com.tnyx.wear.presentation.settings

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
            modifier = modifier.fillMaxSize()
        ) {
            // Header
            item {
                Text(
                    text = "Units Settings",
                    style = WearTypography.title1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 1. Energy Unit
            item {
                Chip(
                    onClick = { showEnergyDialog = true },
                    label = { Text("Energy Unit") },
                    secondaryLabel = { Text(selectedEnergyUnit) },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2. Weight Unit
            item {
                Chip(
                    onClick = { showWeightDialog = true },
                    label = { Text("Weight Unit") },
                    secondaryLabel = { Text(selectedWeightUnit) },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Distance Unit
            item {
                Chip(
                    onClick = { showDistanceDialog = true },
                    label = { Text("Distance Unit") },
                    secondaryLabel = { Text(selectedDistanceUnit) },
                    colors = ChipDefaults.secondaryChipColors(),
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
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Energy Unit",
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
                        label = { Text("Calories (kcal)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Chip(
                        onClick = {
                            selectedEnergyUnit = "kJ"
                            showEnergyDialog = false
                        },
                        label = { Text("Kilojoules (kJ)") },
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
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Weight Unit",
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
                        label = { Text("Kilograms (kg)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Chip(
                        onClick = {
                            selectedWeightUnit = "lbs"
                            showWeightDialog = false
                        },
                        label = { Text("Pounds (lbs)") },
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
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Distance Unit",
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
                        label = { Text("Kilometers (km)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Chip(
                        onClick = {
                            selectedDistanceUnit = "mi"
                            showDistanceDialog = false
                        },
                        label = { Text("Miles (mi)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
