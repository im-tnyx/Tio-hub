package com.tnyx.features.settings.presentation.app_preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant

@Composable
fun AppPreferencesScreen(
    state: AppPreferencesUiState,
    onAction: (AppPreferencesAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AppPreferencesTopBar(
                onBack = { onAction(AppPreferencesAction.BackClicked) }
            )
        },
        containerColor = TnyxTheme.colors.background,
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { SectionHeader(title = "NOTIFICATIONS & REMINDERS") }

            item {
                TnyxCard(
                    variant = TnyxCardVariant.Normal,
                    padding = 0.dp,
                ) {
                    Column {
                        ToggleRow(
                            label = "Status",
                            value = state.notificationsEnabled,
                            onChanged = { onAction(AppPreferencesAction.NotificationsToggled) },
                        )
                        CardDivider()
                        ValueRow(label = "Waking time", value = state.wakingTime)
                        CardDivider()
                        ValueRow(label = "Sleeping time", value = state.sleepingTime)
                        CardDivider()
                        ValueRow(
                            label = "Frequency",
                            value = if (state.frequency == 1) {
                                "1 time / day"
                            } else {
                                "${state.frequency} times / day"
                            },
                            trailingIcon = Icons.Rounded.KeyboardArrowDown,
                        )
                        CardDivider()
                        InfoTextRow(
                            text = "Configure when and how often you'd like to receive personalized Tio insights."
                        )
                    }
                }
            }

            item {
                TnyxCard(
                    variant = TnyxCardVariant.Normal,
                    padding = 0.dp,
                ) {
                    Column {
                        ExpandableToggleRow(
                            label = "Reminders",
                            description = "Reminders at the right time to support healthy habits.",
                            value = state.remindersEnabled,
                            isExpanded = state.remindersExpanded,
                            onChanged = { onAction(AppPreferencesAction.RemindersToggled) },
                            onExpandToggle = {
                                onAction(AppPreferencesAction.RemindersExpandedToggled)
                            },
                        )
                        if (state.remindersExpanded) {
                            CardDivider(startPadding = 32.dp)
                            SubToggleRow(
                                label = "Nutrition",
                                value = state.nutritionReminders,
                                onChanged = {
                                    onAction(AppPreferencesAction.NutritionRemindersToggled)
                                },
                            )
                            SubToggleRow(
                                label = "Workouts",
                                value = state.workoutReminders,
                                onChanged = {
                                    onAction(AppPreferencesAction.WorkoutRemindersToggled)
                                },
                            )
                            SubToggleRow(
                                label = "Hydration",
                                value = state.hydrationReminders,
                                onChanged = {
                                    onAction(AppPreferencesAction.HydrationRemindersToggled)
                                },
                            )
                            SubToggleRow(
                                label = "Recovery",
                                value = state.recoveryReminders,
                                onChanged = {
                                    onAction(AppPreferencesAction.RecoveryRemindersToggled)
                                },
                            )
                            SubToggleRow(
                                label = "Routines",
                                value = state.routinesReminders,
                                onChanged = {
                                    onAction(AppPreferencesAction.RoutinesRemindersToggled)
                                },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            item { SectionHeader(title = "PERSONALIZATION") }

            item {
                TnyxCard(
                    variant = TnyxCardVariant.Normal,
                    padding = 0.dp,
                ) {
                    Column {
                        ValueRow(label = "Theme", value = state.theme)
                        CardDivider()
                        ValueRow(label = "Language", value = state.language)
                        CardDivider()
                        ValueRow(label = "Unit System", value = state.unitSystemSummary)
                        CardDivider()
                        ValueRow(
                            label = "First Day of Week",
                            value = state.firstDayOfWeek,
                            onClick = { onAction(AppPreferencesAction.FirstDayOfWeekClicked) }
                        )
                        CardDivider()
                        ValueRow(
                            label = "Bottom navigation",
                            value = "Customize",
                            leadingIcon = Icons.Rounded.Tune,
                            trailingIcon = Icons.Rounded.ChevronRight,
                            onClick = {
                                onAction(AppPreferencesAction.BottomNavigationClicked)
                            },
                        )
                    }
                }
            }

            item {
                TnyxCard(
                    variant = TnyxCardVariant.Normal,
                    padding = 0.dp,
                ) {
                    Column {
                        ToggleRow(
                            label = "Sound Effects",
                            value = state.soundEffects,
                            onChanged = {
                                onAction(AppPreferencesAction.SoundEffectsToggled)
                            },
                        )
                        if (state.soundEffects) {
                            CardDivider()
                            VolumeRow(
                                label = "Sound Volume",
                                value = state.soundVolume,
                                onChanged = {
                                    onAction(AppPreferencesAction.SoundVolumeChanged(it))
                                },
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // --- First Day of Week Selection BottomSheet ---
        FirstDayOfWeekBottomSheet(
            visible = state.showFirstDayOfWeekBottomSheet,
            selectedDay = state.firstDayOfWeek,
            onDaySelected = { onAction(AppPreferencesAction.FirstDayOfWeekSelected(it)) },
            onDismissRequest = { onAction(AppPreferencesAction.FirstDayOfWeekBottomSheetDismissed) }
        )
    }
}

@Composable
private fun FirstDayOfWeekBottomSheet(
    visible: Boolean,
    selectedDay: String,
    onDaySelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val options = remember { listOf("Sunday", "Monday", "Saturday") }

    com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = "First Day of Week",
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            options.forEach { day ->
                val isSelected = selectedDay.equals(day, ignoreCase = true)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { onDaySelected(day) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (day == "Sunday") "Sunday (Default)" else day,
                        style = TnyxTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TnyxTheme.colors.primary else TnyxTheme.colors.textPrimary,
                    )

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = TnyxTheme.colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppPreferencesTopBar(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding()
    ) {
        com.tnyx.core.ui.components.layouts.TnyxScreenHeader(
            title = "App Settings",
            size = com.tnyx.core.theme.tokens.components.TnyxHeaderSize.Standard,
            uppercaseTitle = false,
            navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
            onNavigationClick = onBack
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TnyxTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textMuted,
        )
        Spacer(modifier = Modifier.size(12.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = TnyxTheme.colors.textMuted.copy(alpha = 0.18f),
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    value: Boolean,
    onChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        PreferenceSwitch(checked = value, onCheckedChange = onChanged)
    }
}

@Composable
private fun ExpandableToggleRow(
    label: String,
    description: String,
    value: Boolean,
    isExpanded: Boolean,
    onChanged: (Boolean) -> Unit,
    onExpandToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = TnyxTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary,
            )
            Text(
                text = description,
                style = TnyxTheme.typography.labelSmall,
                color = TnyxTheme.colors.textMuted,
            )
        }
        PreferenceSwitch(checked = value, onCheckedChange = onChanged)
        IconButton(onClick = onExpandToggle) {
            Icon(
                imageVector = if (isExpanded) {
                    Icons.Rounded.KeyboardArrowUp
                } else {
                    Icons.Rounded.KeyboardArrowDown
                },
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = TnyxTheme.colors.textMuted,
            )
        }
    }
}

@Composable
private fun SubToggleRow(
    label: String,
    value: Boolean,
    onChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = TnyxTheme.typography.bodySmall,
            color = TnyxTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        PreferenceSwitch(checked = value, onCheckedChange = onChanged)
    }
}

@Composable
private fun PreferenceSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = TnyxTheme.colors.primary,
            checkedTrackColor = TnyxTheme.colors.primary.copy(alpha = 0.3f),
            uncheckedThumbColor = TnyxTheme.colors.textMuted,
            uncheckedTrackColor = TnyxTheme.colors.textMuted.copy(alpha = 0.2f),
        ),
    )
}

@Composable
private fun ValueRow(
    label: String,
    value: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = TnyxTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.size(12.dp))
        }
        Text(
            text = label,
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = TnyxTheme.typography.labelMedium,
            color = TnyxTheme.colors.textSecondary,
        )
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.size(10.dp))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = TnyxTheme.colors.textMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun VolumeRow(
    label: String,
    value: Float,
    onChanged: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = TnyxTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textSecondary,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeMute,
                contentDescription = null,
                tint = TnyxTheme.colors.textMuted,
                modifier = Modifier.size(20.dp),
            )
            Slider(
                value = value,
                onValueChange = onChanged,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = TnyxTheme.colors.primary,
                    activeTrackColor = TnyxTheme.colors.primary,
                    inactiveTrackColor = TnyxTheme.colors.textMuted.copy(alpha = 0.2f),
                ),
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = null,
                tint = TnyxTheme.colors.textMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun InfoTextRow(text: String) {
    Text(
        text = text,
        style = TnyxTheme.typography.labelSmall,
        color = TnyxTheme.colors.textMuted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    )
}

@Composable
private fun CardDivider(startPadding: androidx.compose.ui.unit.Dp = 16.dp) {
    HorizontalDivider(
        modifier = Modifier.padding(start = startPadding),
        color = TnyxTheme.colors.textMuted.copy(alpha = 0.16f),
    )
}
