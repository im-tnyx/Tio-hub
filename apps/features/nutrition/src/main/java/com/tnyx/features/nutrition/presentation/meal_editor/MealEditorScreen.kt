package com.tnyx.features.nutrition.presentation.meal_editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.inputs.CupertinoDateTimePicker
import com.tnyx.core.ui.components.sheets.ImageSourceBottomSheet
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
    val isExistingMeal = state.meal.id.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit your meal",
                        style = TnyxTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TnyxTheme.colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(MealEditorAction.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TnyxTheme.colors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TnyxTheme.colors.background,
                    titleContentColor = TnyxTheme.colors.textPrimary,
                )
            )
        },
        bottomBar = {
            MealEditorBottomBar(
                category = state.meal.type,
                isExistingMeal = isExistingMeal,
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
                .padding(TnyxTheme.dimens.SpaceM)
        ) {
            MealIdentityEditor(
                meal = state.meal,
                photoModel = state.photoPreviewBytes ?: state.meal.imageUrl,
                enabled = !state.isLoading && !state.isSaving,
                onPhotoClicked = { onAction(MealEditorAction.PhotoClicked) },
                onNameEditClicked = { onAction(MealEditorAction.EditNameRequested) },
                onServingCountClicked = { onAction(MealEditorAction.ServingCountEditorRequested) },
                onServingSizeClicked = { onAction(MealEditorAction.ServingEditorRequested) },
            )

            state.errorMessage
                ?.takeUnless { it == "Enter a meal name." }
                ?.let { message ->
                    Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))
                    Text(
                        text = message,
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.error,
                    )
                }

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = TnyxTheme.colors.primary,
                    trackColor = TnyxTheme.colors.surfaceVariant,
                )
            }
            
            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

            MealNutritionSummary(meal = state.meal)

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meal items",
                    style = TnyxTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TnyxTheme.colors.textPrimary
                )
                
                TnyxCard(
                    variant = TnyxCardVariant.Outlined,
                    padding = TnyxTheme.dimens.SpaceS,
                    onClick = { onAction(MealEditorAction.AddItemClicked) },
                ) {
                    Text(
                        text = "+ Add item",
                        style = TnyxTheme.typography.labelLarge,
                        color = TnyxTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceS))

            if (state.meal.items.isEmpty()) {
                TnyxCard(
                    variant = TnyxCardVariant.Normal,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "No items added yet. Use Add item to search foods.",
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.textSecondary,
                    )
                }
            } else {
                TnyxCard(
                    variant = TnyxCardVariant.Outlined,
                    padding = TnyxTheme.dimens.SpaceNone,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        state.meal.items.forEachIndexed { index, item ->
                            MealItemTile(
                                item = item,
                                onDelete = { onAction(MealEditorAction.ItemDeleted(item.id)) },
                                onTap = { onAction(MealEditorAction.ItemClicked(item)) },
                            )
                            if (index < state.meal.items.lastIndex) {
                                HorizontalDivider(
                                    color = TnyxTheme.components.card.outlinedBorderColor,
                                    modifier = Modifier.padding(start = TnyxTheme.dimens.SpaceM),
                                )
                            }
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
    onNameEditClicked: () -> Unit,
    onServingCountClicked: () -> Unit,
    onServingSizeClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceSM),
    ) {
        TnyxCard(
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight(),
            variant = TnyxCardVariant.Normal,
            padding = TnyxTheme.dimens.SpaceNone,
            onClick = onPhotoClicked,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (photoModel != null) {
                    AsyncImage(
                        model = photoModel,
                        contentDescription = "Meal photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier
                            .size(TnyxTheme.dimens.IconL)
                            .align(Alignment.Center),
                        tint = TnyxTheme.colors.textSecondary,
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = TnyxTheme.colors.surfaceContainerHigh,
                ) {
                    Text(
                        text = if (photoModel == null) "Add photo" else "Change photo",
                        modifier = Modifier.padding(
                            horizontal = TnyxTheme.dimens.SpaceS,
                            vertical = TnyxTheme.dimens.SpaceXS,
                        ).fillMaxWidth(),
                        style = TnyxTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TnyxTheme.colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(0.62f),
            verticalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXS),
            ) {
                ServingValueCard(
                    label = "Servings",
                    value = meal.servingSize.toServingCountLabel(),
                    contentDescription = "Edit servings",
                    onClick = onServingCountClicked,
                    modifier = Modifier.weight(1f),
                )
                ServingValueCard(
                    label = "Serving size",
                    value = meal.servingsDescription.ifBlank { "1 serving" },
                    contentDescription = "Edit serving size",
                    onClick = onServingSizeClicked,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ServingValueCard(
    label: String,
    value: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TnyxCard(
        modifier = modifier,
        variant = TnyxCardVariant.Outlined,
        padding = TnyxTheme.dimens.SpaceS,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = TnyxTheme.typography.labelSmall,
                    color = TnyxTheme.colors.textSecondary,
                    maxLines = 1,
                )
                Text(
                    text = value,
                    style = TnyxTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TnyxTheme.colors.textPrimary,
                    maxLines = 1,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = contentDescription,
                tint = TnyxTheme.colors.textSecondary,
                modifier = Modifier.size(TnyxTheme.dimens.IconS),
            )
        }
    }
}

@Composable
private fun MealNutritionSummary(meal: NutritionMeal) {
    TnyxCard(
        variant = TnyxCardVariant.Normal,
        padding = TnyxTheme.dimens.SpaceSM,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            NutritionMetric(
                label = "Fat",
                value = "${meal.totalFats.toNutrientLabel()}g",
                color = TnyxTheme.colors.nutrition.fats,
                modifier = Modifier.weight(1f),
            )
            NutritionMetric(
                label = "Protein",
                value = "${meal.totalProtein.toNutrientLabel()}g",
                color = TnyxTheme.colors.nutrition.protein,
                modifier = Modifier.weight(1f),
            )
            NutritionMetric(
                label = "Fiber",
                value = "${meal.totalFiber.toNutrientLabel()}g",
                color = TnyxTheme.colors.success,
                modifier = Modifier.weight(1f),
            )
        }
    }
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
        color = TnyxTheme.colors.surface,
        tonalElevation = 8.dp,
        border = BorderStroke(0.5.dp, TnyxTheme.colors.textPrimary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Category & Date Row
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
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
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
                        onDismissRequest = { showDropdown = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = cat,
                                        style = TnyxTheme.typography.bodyMedium.copy(
                                            fontWeight = if (cat == category) FontWeight.Bold else FontWeight.Normal
                                        ),
                                    )
                                },
                                onClick = {
                                    onCategoryChanged(cat)
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }

                // Date Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable(onClick = onLogDateClicked),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Date",
                        modifier = Modifier.size(16.dp),
                        tint = TnyxTheme.colors.textSecondary,
                    )
                    Text(
                        text = logDateTime.toLogDateLabel(),
                        style = TnyxTheme.typography.bodySmall,
                        color = TnyxTheme.colors.textSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row (Delete/Cancel + Save)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, if (isExistingMeal) TnyxTheme.colors.error.copy(alpha = 0.4f) else TnyxTheme.colors.textPrimary.copy(alpha = 0.15f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isExistingMeal) TnyxTheme.colors.error else TnyxTheme.colors.textPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isExistingMeal) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = if (isExistingMeal) "Delete" else "Cancel",
                            style = TnyxTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                TnyxPrimaryButton(
                    text = if (isSaving) "Saving..." else "Save Meal",
                    onPressed = onSave,
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                )
            }
        }
    }
}

private fun LocalDateTime.toLogDateLabel(): String {
    return if (toLocalDate() == java.time.LocalDate.now()) {
        "Today"
    } else {
        format(DateTimeFormatter.ofPattern("d MMM", Locale.US))
    }
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

