package com.tnyx.features.nutrition.presentation.targets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FoodBank
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SetMeal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.sp
import com.tnyx.features.nutrition.R
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.inputs.SleepScheduleBottomSheet


// ─────────────────────────────────────────────────────────────────────────────
// Screen root
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NutritionTargetsScreen(
    state: NutritionTargetsUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (NutritionTargetsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TnyxTheme.colors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            NutritionTargetsTopBar(
                onBack = { onAction(NutritionTargetsAction.BackClicked) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = TnyxTheme.dimens.SpaceM,
                    top = TnyxTheme.dimens.SpaceS,
                    end = TnyxTheme.dimens.SpaceM,
                    bottom = TnyxTheme.dimens.SpaceM
                ),
                verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM)
            ) {
                item {
                    Text(
                        text = "Current Targets",
                        style = TnyxTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TnyxTheme.colors.textPrimary
                    )
                }
                item {
                    CaloriesCard(
                        state = state,
                        onDynamicCaloriesChanged = {
                            onAction(NutritionTargetsAction.DynamicCaloriesChanged(it))
                        },
                        onEdit = { onAction(NutritionTargetsAction.EditTargetClicked(it)) }
                    )
                }
                item {
                    MacrosCard(
                        state = state,
                        onEdit = { onAction(NutritionTargetsAction.EditTargetClicked(it)) }
                    )
                }
                item {
                    HydrationCard(
                        state = state,
                        onEdit = { onAction(NutritionTargetsAction.EditTargetClicked(it)) }
                    )
                }
                // --- नया: Body & Activity Card ---
                item {
                    BodyActivityCard(
                        state = state,
                        onEdit = { onAction(NutritionTargetsAction.EditTargetClicked(it)) }
                    )
                }

                // --- नया: Recovery (Sleep) Card ---
                item {
                    RecoveryCard(
                        state = state,
                        onEdit = { onAction(NutritionTargetsAction.EditTargetClicked(it)) }
                    )
                }
                item {
                    OptimizeCard(
                        onRecalculate = { onAction(NutritionTargetsAction.RecalculateClicked) }
                    )
                }
                //item { Spacer(modifier = Modifier.navigationBarsPadding()) }
            }

            if (state.isLoading || state.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TnyxTheme.colors.background.copy(alpha = 0.56f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TnyxTheme.colors.primary)
                }
            }
        }
    }
    TargetEditDialog(state = state, onAction = onAction)
    SleepScheduleBottomSheet(
        visible = state.activeEditField == NutritionTargetField.SleepSchedule,
        sleepTime = state.formattedSleepTime,
        wakeTime = state.formattedWakeTime,
        onDismissRequest = { onAction(NutritionTargetsAction.EditDismissed) },
        onSave = { sleepTime, wakeTime ->
            onAction(
                NutritionTargetsAction.SleepScheduleSaved(
                    sleepTime = sleepTime,
                    wakeTime = wakeTime,
                ),
            )
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Top bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NutritionTargetsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = TnyxTheme.dimens.SpaceS)
            .height(TnyxTheme.dimens.ButtonHeightLarge),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // background और size को हटा दिया गया है
        IconButton(
            onClick = onBack
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = TnyxTheme.colors.textPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))

        Text(
            text = "Nutrition Targets",
            style = TnyxTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// Card 1 — Calories
// Flutter: TnyxCard(padding: EdgeInsets.all(paddingCard)) with full content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CaloriesCard(
    state: NutritionTargetsUiState,
    onDynamicCaloriesChanged: (Boolean) -> Unit,
    onEdit: (NutritionTargetField) -> Unit
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    TnyxCard(variant = TnyxCardVariant.Normal, padding = TnyxTheme.dimens.SpaceM) {
        Column {
            // Header: icon  |  title  |  "Variable" or value+edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. यहाँ Drawable से apple_outline.png का उपयोग किया गया है
                Icon(
                    painter = painterResource(id = R.drawable.apple_outline),
                    contentDescription = "Apple Icon",
                    tint = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                Text(
                    text = "Calories",
                    style = TnyxTheme.typography.titleMedium,
                    color = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (state.dynamicCaloriesEnabled) {
                    Text(
                        text = "Variable",
                        style = TnyxTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = TnyxTheme.colors.textMuted
                    )
                } else {
                    ValueWithEdit(
                        value = state.caloriesTarget.toString(),
                        unit = "cals",
                        onEdit = { onEdit(NutritionTargetField.Calories) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))

            // Dynamic toggle row: label + info icon | Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dynamic Calories",
                        style = TnyxTheme.typography.bodyLarge,
                        color = TnyxTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceXS))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { showInfoDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Dynamic calories info",
                            tint = TnyxTheme.colors.textMuted.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                // 2. Switch में thumbContent के ज़रिए Check मार्क
                Switch(
                    checked = state.dynamicCaloriesEnabled,
                    onCheckedChange = onDynamicCaloriesChanged,
                    thumbContent = if (state.dynamicCaloriesEnabled) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                tint = TnyxTheme.colors.primary // अपनी थीम के हिसाब से रंग बदल सकते हैं
                            )
                        }
                    } else null,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = TnyxTheme.colors.primary,
                        checkedThumbColor = TnyxTheme.colors.onPrimary,
                        uncheckedTrackColor = TnyxTheme.colors.surfaceVariant,
                        uncheckedThumbColor = TnyxTheme.colors.textMuted
                    )
                )
            }

            // Surplus row — only when dynamic is ON
            if (state.dynamicCaloriesEnabled) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = TnyxTheme.dimens.SpaceM),
                    thickness = 0.5.dp,
                    color = TnyxTheme.colors.textPrimary.copy(alpha = 0.08f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calorie Surplus Target",
                        style = TnyxTheme.typography.bodyLarge,
                        color = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    ValueWithEdit(
                        value = state.calorieSurplusTarget.toString(),
                        unit = "cals",
                        onEdit = { onEdit(NutritionTargetField.CalorieSurplus) }
                    )
                }
            }
        }
    }

    if (showInfoDialog) {
        DynamicCaloriesInfoDialog(onDismiss = { showInfoDialog = false })
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// Card 2 — Macros (protein / carbs / fat / fiber)
// Flutter: TnyxCard(padding: EdgeInsets.zero) with _TargetItem rows
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MacrosCard(
    state: NutritionTargetsUiState,
    onEdit: (NutritionTargetField) -> Unit
) {
    TnyxCard(variant = TnyxCardVariant.Normal, padding = 0.dp) {
        Column {
            TargetItem(
                painter = painterResource(id = R.drawable.ic_egg),
                title = "Protein",
                value = "${state.proteinTarget.toCleanString()} g",
                onEdit = { onEdit(NutritionTargetField.Protein) }
            )
            IndentDivider()
            TargetItem(
                painter = painterResource(id = R.drawable.ic_carbs),
                title = "Carbs",
                value = "${state.carbsTarget.toCleanString()} g",
                onEdit = { onEdit(NutritionTargetField.Carbs) }
            )
            IndentDivider()
            TargetItem(
                painter = painterResource(id = R.drawable.ic_fat),
                title = "Fat",
                value = "${state.fatTarget.toCleanString()} g",
                onEdit = { onEdit(NutritionTargetField.Fat) }
            )
            IndentDivider()
            TargetItem(
                painter = painterResource(id = R.drawable.ic_fiber),
                title = "Fiber",
                value = "${state.fiberTarget.toCleanString()} g",
                onEdit = { onEdit(NutritionTargetField.Fiber) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card 3 — Hydration
// Flutter: TnyxCard(padding: EdgeInsets.zero) — Water + GlassSize
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HydrationCard(
    state: NutritionTargetsUiState,
    onEdit: (NutritionTargetField) -> Unit
) {
    TnyxCard(variant = TnyxCardVariant.Normal, padding = 0.dp) {
        Column {
            TargetItem(
                painter = painterResource(id = R.drawable.ic_water),
                title = "Water",
                value = "${state.waterTargetLitres.toCleanString()} litres",
                onEdit = { onEdit(NutritionTargetField.Water) }
            )
            IndentDivider()
            // Glass size — full row is clickable (Flutter _buildValueEditRow)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEdit(NutritionTargetField.GlassSize) }
                    .padding(TnyxTheme.dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalDrink,
                    contentDescription = null,
                    tint = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                Text(
                    text = "Glass Size",
                    style = TnyxTheme.typography.bodyLarge,
                    color = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${state.glassSizeMl} ml",
                    style = TnyxTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                EditIconButton(onClick = { onEdit(NutritionTargetField.GlassSize) })
            }
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// Card: Body & Activity (Steps & Target Weight)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BodyActivityCard(
    state: NutritionTargetsUiState,
    onEdit: (NutritionTargetField) -> Unit
) {
    TnyxCard(variant = TnyxCardVariant.Normal, padding = 0.dp) {
        Column {
            TargetItem(
                // ध्यान दें: R.drawable.ic_step की जगह आप अपनी स्टेप वाली png/xml लगा लें
                painter = painterResource(id = R.drawable.ic_step),
                title = "Steps",
                value = "${state.stepsTarget} steps",
                onEdit = { onEdit(NutritionTargetField.Steps) }
            )
            IndentDivider()
            TargetItem(
                // ध्यान दें: R.drawable.ic_weight की जगह अपनी वेट वाली png/xml लगा लें
                painter = painterResource(id = R.drawable.ic_weight),
                title = "Target Weight",
                value = "${state.targetWeight} kg",
                onEdit = { onEdit(NutritionTargetField.TargetWeight) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card: Recovery (Sleep Schedule)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecoveryCard(
    state: NutritionTargetsUiState,
    onEdit: (NutritionTargetField) -> Unit
) {
    TnyxCard(variant = TnyxCardVariant.Normal, padding = TnyxTheme.dimens.SpaceM) {
        Column {
            // Header: Icon + Title + Total Hours
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bedtime,
                    contentDescription = "Sleep Schedule",
                    tint = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                Text(
                    text = "Sleep Schedule",
                    style = TnyxTheme.typography.titleMedium,
                    color = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${state.sleepTargetHours} hrs",
                    style = TnyxTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

            // Footer: Time Range ("11:00 pm - 7:00 am") + EditIconButton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.formattedSleepTime} - ${state.formattedWakeTime}",
                    style = TnyxTheme.typography.titleMedium,
                    color = TnyxTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                EditIconButton(onClick = { onEdit(NutritionTargetField.SleepSchedule) })
            }
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// Card 4 — Optimize / Recalculate
// Has recalculate.png decorative image on the right side
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OptimizeCard(onRecalculate: () -> Unit) {
    val fadeColor = TnyxTheme.colors.surfaceVariant

    TnyxCard(variant = TnyxCardVariant.Normal, padding = 0.dp) {
        // 1. Box में height(IntrinsicSize.Min) जोड़ा गया है
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {

            // Decorative right-side image with left fade
            Image(
                painter = painterResource(id = R.drawable.recalculate),
                contentDescription = null,
                contentScale = ContentScale.Crop, // Crop से इमेज बिना स्ट्रेच हुए पूरी जगह में फिट हो जाएगी
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(130.dp)
                    .fillMaxHeight() // 2. फिक्स height की जगह fillMaxHeight() का उपयोग किया है
                    .clip(
                        RoundedCornerShape(
                            topEnd = TnyxTheme.dimens.RadiusS,
                            bottomEnd = TnyxTheme.dimens.RadiusS
                        )
                    )
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0f to fadeColor,
                                0.7f to fadeColor.copy(alpha = 0f)
                            )
                        )
                    }
            )

            // Text + button on the left
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .padding(TnyxTheme.dimens.SpaceM)
            ) {
                Column {
                    Text(
                        text = "Optimize Targets",
                        style = TnyxTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = TnyxTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
                    Text(
                        text = "Unsure about your target? Just answer a few quick questions and we'll recalculate them for you",
                        style = TnyxTheme.typography.labelLarge,
                        color = TnyxTheme.colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

                TnyxSecondaryButton(
                    text = "Recalculate",
                    onPressed = onRecalculate,
                    expand = false,
                    modifier = Modifier.width(150.dp)
                )
            }
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// Shared: _TargetItem  ← Flutter pattern exactly
// icon(24dp) | SpaceM | title(weight=1, bodyLarge) | value(titleMedium bold) | SpaceM | EditIconButton
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TargetItem(

    painter: Painter,
    title: String,
    value: String,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(TnyxTheme.dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painter,
            //imageVector = icon,
            contentDescription = null,
            tint = TnyxTheme.colors.textPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
        Text(
            text = title,
            style = TnyxTheme.typography.bodyLarge,
            color = TnyxTheme.colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = TnyxTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
        EditIconButton(onClick = onEdit)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared: _ValueWithEdit  ← Flutter — value  unit  EditIconButton inline
// Used in calorie card header (trailing widget)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ValueWithEdit(
    value: String,
    unit: String,
    onEdit: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = value,
            style = TnyxTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TnyxTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = unit,
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textMuted
        )
        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
        EditIconButton(onClick = onEdit)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared: _EditIconButton  ← Flutter: Container 36×36 circular, surfaceContainer bg, edit 16dp
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditIconButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(TnyxTheme.colors.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Edit",
            tint = TnyxTheme.colors.textSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared: indent divider  ← Flutter: Divider(height: 1, indent: 56)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IndentDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        thickness = 0.5.dp,
        color = TnyxTheme.colors.textPrimary.copy(alpha = 0.08f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Dynamic Calories info dialog  ← Flutter: _showDynamicCaloriesInfo()
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DynamicCaloriesInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TnyxTheme.colors.surfaceRaised,
        titleContentColor = TnyxTheme.colors.textPrimary,
        textContentColor = TnyxTheme.colors.textPrimary,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
                Text(
                    text = "Dynamic calories",
                    style = TnyxTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceM)) {
                Text(
                    text = "Dynamic calorie adjustment changes your daily calorie target based on activity, so the plan stays aligned with your day.",
                    style = TnyxTheme.typography.bodyMedium,
                    color = TnyxTheme.colors.textMuted
                )
                Text(
                    text = "How it works",
                    style = TnyxTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary
                )
                InfoBullet("When ON, the calorie target adjusts from activity signals like steps, cardio, or workout volume.")
                InfoBullet("When OFF, the calorie target stays fixed and follows the base target you set manually.")
                InfoBullet("Use it if you track activity in the app. If you do not, keeping it OFF is usually simpler.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Got it",
                    style = TnyxTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary
                )
            }
        }
    )
}

@Composable
private fun InfoBullet(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(TnyxTheme.colors.textMuted)
        )
        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceM))
        Text(
            text = text,
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textMuted,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TargetEditDialog(
    state: NutritionTargetsUiState,
    onAction: (NutritionTargetsAction) -> Unit
) {
    val field = state.activeEditField ?: return
    if (field == NutritionTargetField.SleepSchedule) return

    AlertDialog(
        onDismissRequest = { onAction(NutritionTargetsAction.EditDismissed) },
        containerColor = TnyxTheme.colors.surfaceRaised,
        titleContentColor = TnyxTheme.colors.textPrimary,
        textContentColor = TnyxTheme.colors.textPrimary,
        title = {
            Text(
                text = field.title,
                style = TnyxTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = state.editValue,
                onValueChange = { onAction(NutritionTargetsAction.EditValueChanged(it)) },
                label = { Text(field.unit) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TnyxPrimaryButton(
                text = "Save",
                onPressed = { onAction(NutritionTargetsAction.EditSaved) }
            )
        },
        dismissButton = {
            TextButton(onClick = { onAction(NutritionTargetsAction.EditDismissed) }) {
                Text(text = "Cancel")
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Utilities
// ─────────────────────────────────────────────────────────────────────────────



private fun Double.toCleanString(): String =
    if (this % 1.0 == 0.0) toInt().toString() else String.format("%.1f", this)
