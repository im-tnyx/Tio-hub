package com.tnyx.features.nutrition.presentation.meal_editor

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryVariant
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.inputs.CupertinoDateTimePicker
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader
import com.tnyx.core.ui.components.sheets.ImageSourceBottomSheet
import com.tnyx.core.theme.tokens.components.TnyxHeaderSize
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.presentation.meal_editor.widgets.MealNameEditorBottomSheet
import com.tnyx.features.nutrition.presentation.meal_editor.widgets.MealItemTile
import com.tnyx.features.nutrition.presentation.meal_editor.widgets.ServingCountEditorDialog
import com.tnyx.features.nutrition.presentation.meal_editor.widgets.ServingSizeEditorDialog
import coil.compose.AsyncImage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealEditorScreen(
    state: MealEditorUiState,
    onAction: (MealEditorAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            TnyxScreenHeader(
                title = "Edit your meal",
                modifier = Modifier.statusBarsPadding(),
                size = TnyxHeaderSize.Standard,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = { onAction(MealEditorAction.BackClicked) },
                uppercaseTitle = false,
                reserveNavigationSpace = false,
            )
        },
        bottomBar = {
            MealEditorBottomBar(
                category = state.meal.type,
                isExistingMeal = state.meal.id.isNotBlank(),
                isSaving = state.isSaving,
                logDateTime = state.logDateTime,
                onCategoryChanged = { onAction(MealEditorAction.CategoryChanged(it)) },
                onLogDateClicked = { onAction(MealEditorAction.LogDatePickerRequested) },
                onDelete = { onAction(MealEditorAction.DeleteMealClicked) },
                onSave = { onAction(MealEditorAction.SaveClicked) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = TnyxTheme.colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            MealIdentityEditor(
                meal = state.meal,
                photoModel = state.photoPreviewBytes ?: state.meal.imageUrl,
                enabled = !state.isLoading && !state.isSaving,
                onPhotoClicked = { onAction(MealEditorAction.PhotoClicked) },
                onPhotoRemoved = { onAction(MealEditorAction.PhotoRemoved) },
                onNameEditClicked = { onAction(MealEditorAction.EditNameRequested) },
                onServingCountClicked = { onAction(MealEditorAction.ServingCountEditorRequested) },
                onServingSizeClicked = { onAction(MealEditorAction.ServingEditorRequested) },
            )

            state.errorMessage
                ?.takeUnless { it == "Enter a meal name." }
                ?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier.padding(
                            horizontal = TnyxTheme.dimens.SpaceM,
                            vertical = TnyxTheme.dimens.SpaceS,
                        ),
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.error,
                    )
                }

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TnyxTheme.dimens.SpaceM),
                    color = TnyxTheme.colors.primary,
                    trackColor = TnyxTheme.colors.surfaceVariant,
                )
            }
            
            MealNutritionSummary(meal = state.meal)

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM),
                color = TnyxTheme.colors.surfaceContainerHighest,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = TnyxTheme.dimens.SpaceM,
                        top = TnyxTheme.dimens.SpaceM,
                        end = TnyxTheme.dimens.SpaceS,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meal items",
                    style = TnyxTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary
                )
                
                TextButton(onClick = { onAction(MealEditorAction.AddItemClicked) }) {
                    Text(
                        text = "+ Add item",
                        style = TnyxTheme.typography.labelLarge,
                        color = TnyxTheme.colors.info,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))

            if (state.meal.items.isEmpty()) {
                Text(
                    text = "No items added yet. Use Add item to search foods.",
                    modifier = Modifier.padding(
                        horizontal = TnyxTheme.dimens.SpaceM,
                        vertical = TnyxTheme.dimens.SpaceL,
                    ),
                    style = TnyxTheme.typography.bodyMedium,
                    color = TnyxTheme.colors.textSecondary,
                )
            } else {
                Column {
                    state.meal.items.forEachIndexed { index, item ->
                        MealItemTile(
                            item = item,
                            onDelete = { onAction(MealEditorAction.ItemDeleted(item.id)) },
                            onTap = { onAction(MealEditorAction.ItemClicked(item)) },
                        )
                        if (index < state.meal.items.lastIndex) {
                            HorizontalDivider(
                                color = TnyxTheme.colors.surfaceContainerHighest,
                                modifier = Modifier.padding(start = TnyxTheme.dimens.SpaceM),
                            )
                        }
                    }
                }
            }
        }
    }
    CupertinoDateTimePicker(
        visible = state.isLogDatePickerVisible,
        initialDateTime = state.logDateTime,
        minimumDateTime = LocalDateTime.of(2000, 1, 1, 0, 0),
        maximumDateTime = LocalDateTime.now(),
        onDismissRequest = { onAction(MealEditorAction.LogDatePickerDismissed) },
        onDateTimeChanged = { onAction(MealEditorAction.LogDateTimeChanged(it)) },
    )
    ServingSizeEditorDialog(
        visible = state.isServingEditorVisible,
        amount = state.servingAmountInput,
        selectedUnit = state.servingUnitInput,
        unitOptions = state.servingUnitOptions,
        errorMessage = state.servingEditorError,
        onAmountChanged = { onAction(MealEditorAction.ServingAmountChanged(it)) },
        onUnitSelected = { onAction(MealEditorAction.ServingUnitSelected(it)) },
        onDismissRequest = { onAction(MealEditorAction.ServingEditorDismissed) },
        onConfirm = { onAction(MealEditorAction.ServingEditorConfirmed) },
    )
    ServingCountEditorDialog(
        visible = state.isServingCountEditorVisible,
        count = state.servingCountInput,
        errorMessage = state.servingCountError,
        onCountChanged = { onAction(MealEditorAction.ServingCountChanged(it)) },
        onDismissRequest = { onAction(MealEditorAction.ServingCountEditorDismissed) },
        onConfirm = { onAction(MealEditorAction.ServingCountEditorConfirmed) },
    )
    MealNameEditorBottomSheet(
        visible = state.isNameEditorVisible,
        name = state.nameInput,
        errorMessage = state.nameEditorError,
        onNameChanged = { onAction(MealEditorAction.NameEditorInputChanged(it)) },
        onDismissRequest = { onAction(MealEditorAction.NameEditorDismissed) },
        onConfirm = { onAction(MealEditorAction.NameEditorConfirmed) },
    )
    ImageSourceBottomSheet(
        visible = state.isPhotoSourceVisible,
        onDismissRequest = { onAction(MealEditorAction.PhotoSourceDismissed) },
        onCameraClick = { onAction(MealEditorAction.CameraClicked) },
        onGalleryClick = { onAction(MealEditorAction.GalleryClicked) },
        onRemoveClick = if (state.photoPreviewBytes != null || !state.meal.imageUrl.isNullOrBlank()) {
            { onAction(MealEditorAction.PhotoRemoved) }
        } else {
            null
        },
        title = "Meal photo",
        removeText = "Remove photo",
    )
}

@Composable
private fun MealIdentityEditor(
    meal: NutritionMeal,
    photoModel: Any?,
    enabled: Boolean,
    onPhotoClicked: () -> Unit,
    onPhotoRemoved: () -> Unit,
    onNameEditClicked: () -> Unit,
    onServingCountClicked: () -> Unit,
    onServingSizeClicked: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = TnyxTheme.dimens.SpaceM,
                    top = TnyxTheme.dimens.SpaceS,
                    end = TnyxTheme.dimens.SpaceM,
                )
                .aspectRatio(MEAL_PHOTO_ASPECT_RATIO)
                .clip(TnyxTheme.shapes.Material.large)
                .background(TnyxTheme.colors.surfaceContainerLow)
                .clickable(enabled = enabled, onClick = onPhotoClicked),
        ) {
            if (photoModel != null) {
                AsyncImage(
                    model = photoModel,
                    contentDescription = "Meal photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(TnyxTheme.dimens.IconL),
                        tint = TnyxTheme.colors.textSecondary,
                    )
                    Text(
                        text = "Add meal photo",
                        style = TnyxTheme.typography.labelLarge,
                        color = TnyxTheme.colors.textSecondary,
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .padding(TnyxTheme.dimens.SpaceSM)
                    .align(Alignment.TopEnd),
                shape = CircleShape,
                color = TnyxTheme.colors.surfaceContainerHighest,
            ) {
                IconButton(onClick = onPhotoClicked, enabled = enabled) {
                    Icon(
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = if (photoModel == null) "Add meal photo" else "Change meal photo",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }
            }

            if (photoModel != null) {
                Surface(
                    modifier = Modifier
                        .padding(TnyxTheme.dimens.SpaceSM)
                        .align(Alignment.BottomEnd),
                    shape = CircleShape,
                    color = TnyxTheme.colors.surfaceContainerHighest,
                ) {
                    IconButton(onClick = onPhotoRemoved, enabled = enabled) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Remove meal photo",
                            tint = TnyxTheme.colors.error,
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = TnyxTheme.dimens.SpaceM)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TnyxTheme.dimens.SpaceL, bottom = TnyxTheme.dimens.SpaceM),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = meal.name.ifBlank { "Meal name" },
                    modifier = Modifier.weight(1f),
                    style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (meal.name.isBlank()) {
                        TnyxTheme.colors.textSecondary
                    } else {
                        TnyxTheme.colors.textPrimary
                    },
                    maxLines = 2,
                )
                IconButton(
                    onClick = onNameEditClicked,
                    enabled = enabled,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit meal name",
                        tint = TnyxTheme.colors.textPrimary,
                    )
                }
            }

            ServingValueRow(
                label = "Servings",
                value = meal.servingSize.toServingCountLabel(),
                contentDescription = "Edit servings",
                enabled = enabled,
                onClick = onServingCountClicked,
            )
            HorizontalDivider(color = TnyxTheme.colors.surfaceContainerHighest)
            ServingValueRow(
                label = "Serving size",
                value = meal.servingsDescription.ifBlank { "1 serving" },
                contentDescription = "Edit serving size",
                enabled = enabled,
                onClick = onServingSizeClicked,
            )
        }
    }
}

@Composable
private fun ServingValueRow(
    label: String,
    value: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = TnyxTheme.dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textSecondary,
            maxLines = 1,
        )
        Text(
            text = value,
            style = TnyxTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = TnyxTheme.colors.textPrimary,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceXS))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = contentDescription,
            tint = TnyxTheme.colors.textSecondary,
            modifier = Modifier.size(TnyxTheme.dimens.IconM),
        )
    }
}

@Composable
private fun MealNutritionSummary(meal: NutritionMeal) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = TnyxTheme.dimens.SpaceM,
                vertical = TnyxTheme.dimens.SpaceL,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalorieMacroRing(
            meal = meal,
            modifier = Modifier.size(
                TnyxTheme.dimens.SpaceHuge + TnyxTheme.dimens.SpaceS,
            ),
        )
        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceSM))
        NutritionMetric(
            label = "Carbs",
            value = "${meal.totalCarbs.toNutrientLabel()}g",
            color = TnyxTheme.colors.nutrition.carbs,
            modifier = Modifier.weight(1f),
        )
        NutritionMetricDivider()
        NutritionMetric(
            label = "Fat",
            value = "${meal.totalFats.toNutrientLabel()}g",
            color = TnyxTheme.colors.nutrition.fats,
            modifier = Modifier.weight(1f),
        )
        NutritionMetricDivider()
        NutritionMetric(
            label = "Protein",
            value = "${meal.totalProtein.toNutrientLabel()}g",
            color = TnyxTheme.colors.nutrition.protein,
            modifier = Modifier.weight(1f),
        )
        NutritionMetricDivider()
        NutritionMetric(
            label = "Fiber",
            value = "${meal.totalFiber.toNutrientLabel()}g",
            color = TnyxTheme.colors.success,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NutritionMetricDivider() {
    VerticalDivider(
        modifier = Modifier.height(TnyxTheme.dimens.SpaceXXL),
        color = TnyxTheme.colors.surfaceContainerHighest,
    )
}

@Composable
private fun CalorieMacroRing(
    meal: NutritionMeal,
    modifier: Modifier = Modifier,
) {
    val proteinEnergy = meal.totalProtein * PROTEIN_CALORIES_PER_GRAM
    val carbsEnergy = (meal.totalCarbs - meal.totalFiber)
        .coerceAtLeast(0.0) * CARBS_CALORIES_PER_GRAM
    val fatEnergy = meal.totalFats * FAT_CALORIES_PER_GRAM
    val fiberEnergy = meal.totalFiber * FIBER_CALORIES_PER_GRAM
    val totalMacroEnergy = proteinEnergy + carbsEnergy + fatEnergy + fiberEnergy
    val trackColor = TnyxTheme.colors.surfaceContainerHighest
    val strokeWidth = TnyxTheme.dimens.BorderThick
    val segments = listOf(
        proteinEnergy to TnyxTheme.colors.nutrition.protein,
        carbsEnergy to TnyxTheme.colors.nutrition.carbs,
        fatEnergy to TnyxTheme.colors.nutrition.fats,
        fiberEnergy to TnyxTheme.colors.success,
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = trackColor,
                style = Stroke(width = strokeWidth.toPx()),
            )
            if (totalMacroEnergy > 0.0) {
                var startAngle = -90f
                segments.forEach { (energy, color) ->
                    val sweepAngle = (energy / totalMacroEnergy * FULL_CIRCLE_DEGREES).toFloat()
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx()),
                        )
                        startAngle += sweepAngle
                    }
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = meal.totalCalories.toString(),
                style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TnyxTheme.colors.textPrimary,
                maxLines = 1,
            )
            Text(
                text = "kcal",
                style = TnyxTheme.typography.labelSmall,
                color = TnyxTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun NutritionMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = TnyxTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TnyxTheme.colors.textPrimary,
            maxLines = 1,
        )
        Text(
            text = label,
            style = TnyxTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun MealEditorBottomBar(
    category: String,
    isExistingMeal: Boolean,
    isSaving: Boolean,
    logDateTime: LocalDateTime,
    onCategoryChanged: (String) -> Unit,
    onLogDateClicked: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    val categories = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACKS")

    Surface(
        color = TnyxTheme.colors.surfaceRaised,
        shadowElevation = TnyxTheme.elevation.Level4,
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(
                    horizontal = TnyxTheme.dimens.SpaceM,
                    vertical = TnyxTheme.dimens.SpaceSM,
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showDropdown = true }
                            .padding(vertical = TnyxTheme.dimens.SpaceXS),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(TnyxTheme.dimens.IconS),
                            tint = TnyxTheme.colors.nutrition.calories,
                        )
                        Spacer(modifier = Modifier.width(TnyxTheme.dimens.SpaceS))
                        Text(
                            text = category.ifBlank { "BREAKFAST" },
                            style = TnyxTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = TnyxTheme.colors.textPrimary,
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = "Select category",
                            tint = TnyxTheme.colors.textPrimary,
                        )
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        containerColor = Color.Transparent,
                        tonalElevation = TnyxTheme.elevation.None,
                        shadowElevation = TnyxTheme.elevation.None,
                    ) {
                        TnyxCard(
                            variant = TnyxCardVariant.Elevated,
                            padding = TnyxTheme.dimens.SpaceNone,
                        ) {
                            Column {
                                categories.forEachIndexed { index, cat ->
                                    Text(
                                        text = cat,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onCategoryChanged(cat)
                                                showDropdown = false
                                            }
                                            .padding(
                                                horizontal = TnyxTheme.dimens.SpaceM,
                                                vertical = TnyxTheme.dimens.SpaceSM,
                                            ),
                                        style = TnyxTheme.typography.bodyMedium.copy(
                                            fontWeight = if (cat == category) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                        ),
                                        color = if (cat == category) {
                                            TnyxTheme.colors.primary
                                        } else {
                                            TnyxTheme.colors.textPrimary
                                        },
                                    )

                                    if (index < categories.lastIndex) {
                                        HorizontalDivider(
                                            color = TnyxTheme.colors.surfaceContainerHighest,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXS),
                    modifier = Modifier.clickable(onClick = onLogDateClicked),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Date",
                        modifier = Modifier.size(TnyxTheme.dimens.IconXS),
                        tint = TnyxTheme.colors.textSecondary,
                    )
                    Text(
                        text = logDateTime.toLogDateLabel(),
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceSM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TnyxSecondaryButton(
                    text = if (isExistingMeal) "Delete" else "Cancel",
                    onPressed = onDelete,
                    modifier = Modifier.weight(1f),
                    variant = if (isExistingMeal) {
                        TnyxSecondaryVariant.Destructive
                    } else {
                        TnyxSecondaryVariant.Muted
                    },
                    leading = if (isExistingMeal) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(TnyxTheme.dimens.IconXS),
                            )
                        }
                    } else {
                        null
                    },
                )

                TnyxPrimaryButton(
                    text = if (isSaving) "Saving..." else "Save Meal",
                    onPressed = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun LocalDateTime.toLogDateLabel(): String {
    val dateLabel = if (toLocalDate() == java.time.LocalDate.now()) {
        "Today"
    } else {
        format(DateTimeFormatter.ofPattern("d MMM", Locale.US))
    }
    return "$dateLabel, ${format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))}"
}

private fun Double.toNutrientLabel(): String {
    return String.format(Locale.US, "%.1f", this).removeSuffix(".0")
}

private fun Double.toServingCountLabel(): String {
    val amount = String.format(Locale.US, "%.2f", this).trimEnd('0').trimEnd('.')
    return if (this == 1.0) "$amount serving" else "$amount servings"
}

private const val PROTEIN_CALORIES_PER_GRAM = 4.0
private const val CARBS_CALORIES_PER_GRAM = 4.0
private const val FAT_CALORIES_PER_GRAM = 9.0
private const val FIBER_CALORIES_PER_GRAM = 2.0
private const val FULL_CIRCLE_DEGREES = 360.0
private const val MEAL_PHOTO_ASPECT_RATIO = 16f / 9f

