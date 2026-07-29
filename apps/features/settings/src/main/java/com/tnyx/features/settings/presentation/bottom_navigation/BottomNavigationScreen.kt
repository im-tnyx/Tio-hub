package com.tnyx.features.settings.presentation.bottom_navigation

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tnyx.core.R
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.shell.domain.model.MAX_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.MIN_BOTTOM_NAV_TABS
import com.tnyx.core.ui.shell.domain.model.ShellTab

@Composable
fun BottomNavigationScreen(
    state: BottomNavigationUiState,
    onAction: (BottomNavigationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            EditorTopBar(onBack = { onAction(BottomNavigationAction.BackClicked) })
        },
        bottomBar = {
            EditorBottomBar(
                canReset = state.canReset,
                canSave = state.canSave,
                isSaving = state.isSaving,
                onReset = { onAction(BottomNavigationAction.ResetClicked) },
                onSave = { onAction(BottomNavigationAction.SaveClicked) },
            )
        },
        containerColor = TnyxTheme.colors.background,
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = TnyxTheme.colors.warning)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text(
                        text = "Choose the areas you use most. Home always stays first and adapts its summary to your selection.",
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textSecondary,
                    )
                }

                item { NavigationPreview(tabs = state.draftTabs) }

                state.errorMessage?.let { message ->
                    item {
                        ErrorMessage(
                            message = message,
                            onDismiss = { onAction(BottomNavigationAction.DismissError) },
                        )
                    }
                }

                item {
                    SectionHeader(
                        title = "VISIBLE TABS",
                        detail = "${state.draftTabs.size}/$MAX_BOTTOM_NAV_TABS",
                    )
                }

                item {
                    TnyxCard(
                        variant = TnyxCardVariant.Normal,
                        padding = 0.dp,
                    ) {
                        Column {
                            state.draftTabs.forEachIndexed { index, tab ->
                                VisibleTabRow(
                                    tab = tab,
                                    canMoveUp = index > 1,
                                    canMoveDown = index in 1 until state.draftTabs.lastIndex,
                                    canDisable = tab != ShellTab.Home &&
                                        state.draftTabs.size > MIN_BOTTOM_NAV_TABS,
                                    onMoveUp = {
                                        onAction(BottomNavigationAction.MoveTabUp(tab))
                                    },
                                    onMoveDown = {
                                        onAction(BottomNavigationAction.MoveTabDown(tab))
                                    },
                                    onDisable = {
                                        onAction(BottomNavigationAction.ToggleTab(tab))
                                    },
                                )
                                if (index != state.draftTabs.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 56.dp),
                                        color = TnyxTheme.colors.textMuted.copy(alpha = 0.16f),
                                    )
                                }
                            }
                        }
                    }
                }

                if (state.availableTabs.isNotEmpty()) {
                    item { SectionHeader(title = "AVAILABLE TABS") }

                    items(
                        items = state.availableTabs,
                        key = ShellTab::stableId,
                    ) { tab ->
                        AvailableTabRow(
                            tab = tab,
                            enabled = state.draftTabs.size < MAX_BOTTOM_NAV_TABS,
                            onAdd = { onAction(BottomNavigationAction.ToggleTab(tab)) },
                        )
                    }
                }

                item {
                    Text(
                        text = "Keep between $MIN_BOTTOM_NAV_TABS and $MAX_BOTTOM_NAV_TABS tabs. Meal Plan, Library and You are optional; the default remains unchanged.",
                        style = TnyxTheme.typography.labelSmall,
                        color = TnyxTheme.colors.textMuted,
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun EditorTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = TnyxTheme.colors.textPrimary,
            )
        }
        Text(
            text = "Bottom navigation",
            style = TnyxTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun EditorBottomBar(
    canReset: Boolean,
    canSave: Boolean,
    isSaving: Boolean,
    onReset: () -> Unit,
    onSave: () -> Unit,
) {
    Surface(
        color = TnyxTheme.colors.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onReset,
                enabled = canReset,
                modifier = Modifier.weight(1f),
            ) {
                Text("Reset")
            }
            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TnyxTheme.colors.warning,
                    contentColor = TnyxTheme.colors.background,
                ),
            ) {
                Text(if (isSaving) "Saving…" else "Save")
            }
        }
    }
}

@Composable
private fun NavigationPreview(tabs: List<ShellTab>) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = 12.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "PREVIEW",
                style = TnyxTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textMuted,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        TabIcon(tab = tab, modifier = Modifier.size(22.dp))
                        Text(
                            text = tab.displayLabel(),
                            style = TnyxTheme.typography.labelSmall,
                            color = TnyxTheme.colors.textSecondary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VisibleTabRow(
    tab: ShellTab,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canDisable: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDisable: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabIcon(tab = tab, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tab.displayLabel(),
                style = TnyxTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TnyxTheme.colors.textPrimary,
            )
            if (tab == ShellTab.Home) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = TnyxTheme.colors.textMuted,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Fixed first",
                        style = TnyxTheme.typography.labelSmall,
                        color = TnyxTheme.colors.textMuted,
                    )
                }
            }
        }

        if (tab != ShellTab.Home) {
            IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "Move ${tab.displayLabel()} up",
                    tint = if (canMoveUp) TnyxTheme.colors.textSecondary else TnyxTheme.colors.textMuted,
                )
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Move ${tab.displayLabel()} down",
                    tint = if (canMoveDown) TnyxTheme.colors.textSecondary else TnyxTheme.colors.textMuted,
                )
            }
        }

        Switch(
            checked = true,
            onCheckedChange = { onDisable() },
            enabled = canDisable,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TnyxTheme.colors.warning,
                checkedTrackColor = TnyxTheme.colors.warning.copy(alpha = 0.3f),
                disabledCheckedThumbColor = TnyxTheme.colors.textMuted,
                disabledCheckedTrackColor = TnyxTheme.colors.textMuted.copy(alpha = 0.16f),
            ),
        )
    }
}

@Composable
private fun AvailableTabRow(
    tab: ShellTab,
    enabled: Boolean,
    onAdd: () -> Unit,
) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabIcon(tab = tab, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tab.displayLabel(),
                    style = TnyxTheme.typography.bodyMedium,
                    color = TnyxTheme.colors.textPrimary,
                )
                Text(
                    text = tab.supportingLabel(),
                    style = TnyxTheme.typography.labelSmall,
                    color = TnyxTheme.colors.textMuted,
                )
            }
            OutlinedButton(onClick = onAdd, enabled = enabled) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Add")
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit,
) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = 12.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = message,
                style = TnyxTheme.typography.bodySmall,
                color = TnyxTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    detail: String? = null,
) {
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
        Spacer(modifier = Modifier.size(10.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = TnyxTheme.colors.textMuted.copy(alpha = 0.18f),
        )
        if (detail != null) {
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = detail,
                style = TnyxTheme.typography.labelSmall,
                color = TnyxTheme.colors.textMuted,
            )
        }
    }
}

@Composable
private fun TabIcon(
    tab: ShellTab,
    modifier: Modifier = Modifier,
) {
    val outlineIconRes: Int? = when (tab) {
        ShellTab.Home -> R.drawable.ic_nav_home_outlined
        ShellTab.Nutrition -> R.drawable.ic_nav_nutrition_outlined
        ShellTab.MealPlan -> R.drawable.ic_nav_meal_plan_outlined
        ShellTab.Ai -> null
        ShellTab.Workout -> R.drawable.ic_nav_workout_outlined
        ShellTab.WorkoutLibrary -> R.drawable.ic_nav_library_outlined
        ShellTab.Progress -> R.drawable.ic_nav_progress_outlined
        ShellTab.You -> R.drawable.ic_user__outline
    }

    if (outlineIconRes != null) {
        Icon(
            painter = painterResource(id = outlineIconRes),
            contentDescription = null,
            tint = TnyxTheme.colors.textSecondary,
            modifier = modifier,
        )
    } else {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = TnyxTheme.colors.ai,
            modifier = modifier,
        )
    }
}

private fun ShellTab.displayLabel(): String = when (this) {
    ShellTab.Home -> "Home"
    ShellTab.Nutrition -> "Nutrition"
    ShellTab.MealPlan -> "Meal Plan"
    ShellTab.Ai -> "Tio"
    ShellTab.Workout -> "Workout"
    ShellTab.WorkoutLibrary -> "Library"
    ShellTab.Progress -> "Progress"
    ShellTab.You -> "You"
}

private fun ShellTab.supportingLabel(): String = when (this) {
    ShellTab.Home -> "Adaptive summary"
    ShellTab.Nutrition -> "Food logging and targets"
    ShellTab.MealPlan -> "Plans and meal suggestions"
    ShellTab.Ai -> "AI coaching"
    ShellTab.Workout -> "Current training"
    ShellTab.WorkoutLibrary -> "Exercises, routines and programs"
    ShellTab.Progress -> "Trends and achievements"
    ShellTab.You -> "Profile, goals and settings"
}
