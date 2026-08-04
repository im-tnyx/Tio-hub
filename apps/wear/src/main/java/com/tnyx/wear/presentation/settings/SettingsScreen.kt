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
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.tnyx.wear.presentation.components.CircularConfirmButton
import com.tnyx.wear.theme.BackgroundBlack
import com.tnyx.wear.theme.CardBackground
import com.tnyx.wear.theme.ColorSteps
import com.tnyx.wear.theme.TextGray
import com.tnyx.wear.theme.TextWhite
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material.Icon
import com.tnyx.wear.R
import com.tnyx.wear.theme.ColorWater
import com.tnyx.wear.theme.WearTypography

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUnits: () -> Unit,
    onLogoutConfirmed: () -> Unit,
    onOpenOnPhone: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val columnState = rememberColumnState()
    
    // Settings States
    var autoDetectWorkout by remember { mutableStateOf(true) }
    var inactiveAlerts by remember { mutableStateOf(true) }
    var isSyncing by remember { mutableStateOf(false) }
    
    // Dialog/Alert States
    var showLogoutDialog by remember { mutableStateOf(false) }

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundBlack)
        ) {
            // Screen Header
            item {
                Text(
                    text = "Settings",
                    color = TextWhite,
                    style = WearTypography.title1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // 1. Toggle: Auto Detect Workout
            item {
                ToggleChip(
                    checked = autoDetectWorkout,
                    onCheckedChange = { autoDetectWorkout = it },
                    label = { Text("Auto Detect Workout", color = TextWhite) },
                    secondaryLabel = { Text(if (autoDetectWorkout) "On" else "Off", color = TextGray) },
                    toggleControl = {
                        Switch(checked = autoDetectWorkout)
                    },
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedStartBackgroundColor = CardBackground,
                        checkedEndBackgroundColor = CardBackground,
                        uncheckedStartBackgroundColor = CardBackground,
                        uncheckedEndBackgroundColor = CardBackground,
                        checkedToggleControlColor = ColorSteps
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 2. Toggle: Inactive Alerts
            item {
                ToggleChip(
                    checked = inactiveAlerts,
                    onCheckedChange = { inactiveAlerts = it },
                    label = { Text("Inactive Alerts", color = TextWhite) },
                    secondaryLabel = { Text(if (inactiveAlerts) "On" else "Off", color = TextGray) },
                    toggleControl = {
                        Switch(checked = inactiveAlerts)
                    },
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedStartBackgroundColor = CardBackground,
                        checkedEndBackgroundColor = CardBackground,
                        uncheckedStartBackgroundColor = CardBackground,
                        uncheckedEndBackgroundColor = CardBackground,
                        checkedToggleControlColor = ColorSteps
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 3. Selection: Units (Opens Units Settings Sub-menu)
            item {
                Chip(
                    onClick = onNavigateToUnits,
                    label = { Text("Units", color = TextWhite) },
                    secondaryLabel = { Text("kcal • kg • km", color = TextGray) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_units),
                            contentDescription = "Units",
                            tint = ColorSteps,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = CardBackground,
                        contentColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 4. Action: Open on Phone
            item {
                Chip(
                    onClick = onOpenOnPhone,
                    label = { Text("Open on Phone", color = TextWhite) },
                    secondaryLabel = { Text("Launch TNYX on Phone", color = TextGray) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_open_in_phone),
                            contentDescription = "Open on Phone",
                            tint = ColorWater,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = CardBackground,
                        contentColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 5. Action: Sync Now
            item {
                Chip(
                    onClick = {
                        isSyncing = true
                    },
                    label = { Text(if (isSyncing) "Syncing..." else "Sync Now", color = TextWhite) },
                    secondaryLabel = { Text("Last sync: 5m ago", color = TextGray) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sync_vector),
                            contentDescription = "Sync",
                            tint = ColorWater,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = CardBackground,
                        contentColor = TextWhite
                    ),
                    enabled = !isSyncing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 6. Danger Action: Log Out
            item {
                Chip(
                    onClick = { showLogoutDialog = true },
                    label = { Text("Log Out", color = MaterialTheme.colors.error) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = "Log Out",
                            tint = MaterialTheme.colors.error,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = CardBackground,
                        contentColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    // --- DIALOGS ---
    
    // Log Out Confirmation Dialog
    Dialog(
        showDialog = showLogoutDialog,
        onDismissRequest = { showLogoutDialog = false }
    ) {
        Alert(
            title = { Text("Log Out", color = TextWhite, style = WearTypography.title1) },
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
                    Text("✕", color = TextWhite)
                }
            }
        ) {
            Text(
                text = "Are you sure you want to log out from this watch?",
                color = TextGray,
                style = WearTypography.body1
            )
        }
    }
}
