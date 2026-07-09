package com.tnyx.features.settings.presentation.app_preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AppPreferencesTopBar(
                onBack = { onAction(AppPreferencesAction.BackClicked) }
            )
        },
        containerColor = TnyxTheme.colors.background
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader(title = "NOTIFICATIONS & REMINDERS")
            }
            item {
                TnyxCard(
                    variant = TnyxCardVariant.Normal,
                    padding = 0.dp
                ) {
                    Column {
                        ToggleRow(
                            label = "Status",
                            value = state.notificationsEnabled,
                            onChanged = { onAction(AppPreferencesAction.NotificationsToggled) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                        EditValueRow(label = "Waking time", value = state.wakingTime)
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                        EditValueRow(label = "Sleeping time", value = state.sleepingTime)
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                        DropdownValueRow(
                            label = "Frequency",
                            value = if (state.frequency == 1) "1 time / day" else "${state.frequency} times / day"
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                        InfoTextRow(
                            text = "Configure when and how often you'd like to receive personalized AI insights through notifications."
                        )
                    }
                }
            }
            item {
                TnyxCard(
                    variant = TnyxCardVariant.Normal,
                    padding = 0.dp
                ) {
                    Column {
                        ExpandableToggleRow(
                            label = "Reminders",
                            description = "Reminders at the right time to support healthy habits.",
                            value = state.remindersEnabled,
                            isExpanded = state.remindersExpanded,
                            onChanged = { onAction(AppPreferencesAction.RemindersToggled) },
                            onExpandToggle = { onAction(AppPreferencesAction.RemindersExpandedToggled) }
                        )
                        if (state.remindersExpanded) {
                            SubToggleRow(
                                label = "Nutrition",
                                value = state.nutritionReminders,
                                onChanged = { onAction(AppPreferencesAction.NutritionRemindersToggled) }
                            )
                            SubToggleRow(
                                label = "Workouts",
                                value = state.workoutReminders,
                                onChanged = { onAction(AppPreferencesAction.WorkoutRemindersToggled) }
                            )
                            SubToggleRow(
                                label = "Hydration",
                                value = state.hydrationReminders,
                                onChanged = { onAction(AppPreferencesAction.HydrationRemindersToggled) }
                            )
                            SubToggleRow(
                                label = "Recovery",
                                value = state.recoveryReminders,
                                onChanged = { onAction(AppPreferencesAction.RecoveryRemindersToggled) }
                            )
                            SubToggleRow(
                                label = "Routines",
                                value = state.routinesReminders,
                                onChanged = { onAction(AppPreferencesAction.RoutinesRemindersToggled) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            item {
                SectionHeader(title = "PERSONALIZATION")
            }
            item {
                TnyxCard(
                    variant = TnyxCardVariant.Normal,
                    padding = 0.dp
                ) {
                    Column {
                        EditValueRow(label = "Theme", value = state.theme)
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                        EditValueRow(label = "Language", value = state.language)
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                        EditValueRow(label = "Unit System", value = state.unitSystemSummary)
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                        EditValueRow(label = "First Day of Week", value = state.firstDayOfWeek)
                    }
                }
            }
            item {
                TnyxCard(
                    variant = TnyxCardVariant.Normal,
                    padding = 0.dp
                ) {
                    Column {
                        ToggleRow(
                            label = "Sound Effects",
                            value = state.soundEffects,
                            onChanged = { onAction(AppPreferencesAction.SoundEffectsToggled) }
                        )
                        if (state.soundEffects) {
                            HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                            VolumeRow(
                                label = "Sound Volume",
                                value = state.soundVolume,
                                onChanged = { onAction(AppPreferencesAction.SoundVolumeChanged(it)) }
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AppPreferencesTopBar(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                tint = TnyxTheme.colors.textPrimary
            )
        }
        Text(
            text = "App Settings",
            style = TnyxTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary
        )
    }
}

@Composable
private fun SectionHeader(
    title: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TnyxTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textMuted
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = TnyxTheme.colors.textMuted.copy(alpha = 0.18f)
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    value: Boolean,
    onChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = value,
            onCheckedChange = onChanged,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TnyxTheme.colors.warning,
                checkedTrackColor = TnyxTheme.colors.warning.copy(alpha = 0.3f),
                uncheckedThumbColor = TnyxTheme.colors.textMuted,
                uncheckedTrackColor = TnyxTheme.colors.textMuted.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun ExpandableToggleRow(
    label: String,
    description: String,
    value: Boolean,
    isExpanded: Boolean,
    onChanged: (Boolean) -> Unit,
    onExpandToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = TnyxTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary
            )
            Text(
                text = description,
                style = TnyxTheme.typography.labelSmall,
                color = TnyxTheme.colors.textMuted
            )
        }
        Switch(
            checked = value,
            onCheckedChange = onChanged,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TnyxTheme.colors.warning,
                checkedTrackColor = TnyxTheme.colors.warning.copy(alpha = 0.3f),
                uncheckedThumbColor = TnyxTheme.colors.textMuted,
                uncheckedTrackColor = TnyxTheme.colors.textMuted.copy(alpha = 0.2f)
            )
        )
        IconButton(onClick = onExpandToggle) {
            Icon(
                imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = TnyxTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun SubToggleRow(
    label: String,
    value: Boolean,
    onChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TnyxTheme.typography.bodySmall,
            color = TnyxTheme.colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = value,
            onCheckedChange = onChanged,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TnyxTheme.colors.warning,
                checkedTrackColor = TnyxTheme.colors.warning.copy(alpha = 0.3f),
                uncheckedThumbColor = TnyxTheme.colors.textMuted,
                uncheckedTrackColor = TnyxTheme.colors.textMuted.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun EditValueRow(
    label: String,
    value: String
) {
    Surface(
        onClick = {},
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = TnyxTheme.typography.labelMedium,
                color = TnyxTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.width(12.dp))
            ValueActionChip(icon = Icons.Rounded.Edit)
        }
    }
}

@Composable
private fun DropdownValueRow(
    label: String,
    value: String
) {
    Surface(
        onClick = {},
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = TnyxTheme.typography.labelMedium,
                color = TnyxTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.width(12.dp))
            ValueActionChip(icon = Icons.Rounded.KeyboardArrowDown)
        }
    }
}

@Composable
private fun ValueActionChip(
    icon: ImageVector
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TnyxTheme.colors.surfaceVariant.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TnyxTheme.colors.textMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun VolumeRow(
    label: String,
    value: Float,
    onChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = TnyxTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeMute,
                contentDescription = null,
                tint = TnyxTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Slider(
                value = value,
                onValueChange = onChanged,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = TnyxTheme.colors.warning,
                    activeTrackColor = TnyxTheme.colors.warning,
                    inactiveTrackColor = TnyxTheme.colors.textMuted.copy(alpha = 0.2f)
                ),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = null,
                tint = TnyxTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun InfoTextRow(
    text: String
) {
    Text(
        text = text,
        style = TnyxTheme.typography.labelSmall,
        color = TnyxTheme.colors.textMuted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    )
}
