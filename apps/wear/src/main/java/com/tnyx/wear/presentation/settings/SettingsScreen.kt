package com.tnyx.wear.presentation.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.tnyx.wear.presentation.components.CircularConfirmButton
import com.tnyx.wear.theme.WearTypography

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogoutConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val columnState = rememberColumnState()
    
    // Settings States
    var autoDetectWorkout by remember { mutableStateOf(true) }
    var inactiveAlerts by remember { mutableStateOf(true) }
    var isSyncing by remember { mutableStateOf(false) }
    
    // Dialog/Alert States
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showUnitsDialog by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf("kcal") }

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = modifier.fillMaxSize()
        ) {
            // Screen Header
            item {
                Text(
                    text = "Settings",
                    style = WearTypography.title1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // 1. Toggle: Auto Detect Workout
            item {
                ToggleChip(
                    checked = autoDetectWorkout,
                    onCheckedChange = { autoDetectWorkout = it },
                    label = { Text("Auto Detect Workout") },
                    toggleControl = {
                        Switch(
                            checked = autoDetectWorkout,
                            onCheckedChange = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 2. Toggle: Inactive Alerts
            item {
                ToggleChip(
                    checked = inactiveAlerts,
                    onCheckedChange = { inactiveAlerts = it },
                    label = { Text("Inactive Alerts") },
                    toggleControl = {
                        Switch(
                            checked = inactiveAlerts,
                            onCheckedChange = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 3. Selection: Units
            item {
                Chip(
                    onClick = { showUnitsDialog = true },
                    label = { Text("Units") },
                    secondaryLabel = { Text(selectedUnit) },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 4. Action: Sync Now
            item {
                Chip(
                    onClick = {
                        isSyncing = true
                    },
                    label = { Text(if (isSyncing) "Syncing..." else "Sync Now") },
                    secondaryLabel = { Text("Last sync: 5m ago") },
                    colors = ChipDefaults.secondaryChipColors(),
                    enabled = !isSyncing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 5. Danger Action: Log Out
            item {
                Chip(
                    onClick = { showLogoutDialog = true },
                    label = { Text("Log Out", color = androidx.wear.compose.material.MaterialTheme.colors.error) },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    // --- DIALOGS ---
    
    // A. Log Out Confirmation Dialog
    Dialog(
        showDialog = showLogoutDialog,
        onDismissRequest = { showLogoutDialog = false }
    ) {
        Alert(
            title = { Text("Log Out", style = WearTypography.title1) },
            positiveButton = {
                CircularConfirmButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutConfirmed()
                    }
                )
            },
            negativeButton = {
                Button(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Text("✕")
                }
            }
        ) {
            Text(
                text = "Are you sure you want to log out from this watch?",
                style = WearTypography.body1
            )
        }
    }

    // B. Units Selection Dialog
    Dialog(
        showDialog = showUnitsDialog,
        onDismissRequest = { showUnitsDialog = false }
    ) {
        val dialogColumnState = rememberColumnState()
        ScreenScaffold(scrollState = dialogColumnState) {
            ScalingLazyColumn(
                columnState = dialogColumnState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Select Unit",
                        style = WearTypography.title1,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item {
                    Chip(
                        onClick = {
                            selectedUnit = "kcal"
                            showUnitsDialog = false
                        },
                        label = { Text("Calories (kcal)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Chip(
                        onClick = {
                            selectedUnit = "kJ"
                            showUnitsDialog = false
                        },
                        label = { Text("Kilojoules (kJ)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
