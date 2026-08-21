package com.tnyx.features.nutrition.presentation.meal_item_editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.features.nutrition.presentation.meal_item_editor.widgets.IngredientQuantityEditor
import com.tnyx.features.nutrition.presentation.meal_item_editor.widgets.MacroNutrientEditorCard
import com.tnyx.features.nutrition.presentation.meal_item_editor.widgets.MealItemNameBottomSheet
import com.tnyx.features.nutrition.presentation.meal_item_editor.widgets.MicronutrientEditorCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealItemEditorScreen(
    state: MealItemEditorUiState,
    onAction: (MealItemEditorAction) -> Unit,
) {
    val isExistingItem = state.item.id.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isExistingItem) "Edit Ingredient" else "Add Ingredient",
                        style = TnyxTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = TnyxTheme.colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(MealItemEditorAction.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TnyxTheme.colors.textPrimary,
                        )
                    }
                },
                actions = {
                    if (isExistingItem) {
                        IconButton(onClick = { onAction(MealItemEditorAction.ResetClicked) }) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Reset ingredient changes",
                                tint = TnyxTheme.colors.textPrimary,
                            )
                        }
                        IconButton(onClick = { onAction(MealItemEditorAction.RemoveClicked) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Remove ingredient",
                                tint = TnyxTheme.colors.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TnyxTheme.colors.background,
                ),
            )
        },
        bottomBar = {
            IngredientSaveBar(
                enabled = !state.isSaving,
                onSave = { onAction(MealItemEditorAction.SaveClicked) },
            )
        },
        containerColor = TnyxTheme.colors.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TnyxTheme.dimens.SpaceM),
        ) {
            IngredientNameHeader(
                name = state.item.name,
                onEdit = { onAction(MealItemEditorAction.NameEditorRequested) },
            )

            state.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXS))
                Text(
                    text = message,
                    style = TnyxTheme.typography.bodySmall,
                    color = TnyxTheme.colors.error,
                )
            }

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
            IngredientQuantityEditor(
                item = state.item,
                onQuantityChanged = { onAction(MealItemEditorAction.QuantityChanged(it)) },
                onUnitChanged = { onAction(MealItemEditorAction.UnitChanged(it)) },
            )

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            MacroNutrientEditorCard(
                item = state.item,
                onNutrientChanged = { field, value ->
                    onAction(MealItemEditorAction.NutrientChanged(field, value))
                },
            )

            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
            MicronutrientEditorCard(
                item = state.item,
                expanded = state.isMicronutrientsExpanded,
                onToggle = { onAction(MealItemEditorAction.MicronutrientsToggled) },
                onNutrientChanged = { field, value ->
                    onAction(MealItemEditorAction.NutrientChanged(field, value))
                },
                onMicronutrientChanged = { field, value ->
                    onAction(MealItemEditorAction.MicronutrientChanged(field, value))
                },
            )
            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
        }
    }

    MealItemNameBottomSheet(
        visible = state.isNameEditorVisible,
        name = state.nameInput,
        errorMessage = state.nameEditorError,
        onNameChanged = { onAction(MealItemEditorAction.NameEditorInputChanged(it)) },
        onDismissRequest = { onAction(MealItemEditorAction.NameEditorDismissed) },
        onConfirm = { onAction(MealItemEditorAction.NameEditorConfirmed) },
    )
}

@Composable
private fun IngredientNameHeader(
    name: String,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = name.ifBlank { "Ingredient name" },
            modifier = Modifier.weight(1f),
            style = TnyxTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = if (name.isBlank()) {
                TnyxTheme.colors.textSecondary
            } else {
                TnyxTheme.colors.textPrimary
            },
        )
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit ingredient name",
                tint = TnyxTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun IngredientSaveBar(
    enabled: Boolean,
    onSave: () -> Unit,
) {
    Surface(
        color = TnyxTheme.colors.background,
        border = BorderStroke(
            width = TnyxTheme.dimens.BorderThin,
            color = TnyxTheme.colors.textPrimary.copy(alpha = 0.1f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = TnyxTheme.dimens.SpaceM,
                    vertical = TnyxTheme.dimens.SpaceSM,
                ),
        ) {
            TnyxPrimaryButton(
                text = if (enabled) "Save" else "Saving...",
                onPressed = onSave,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                height = TnyxTheme.dimens.ButtonHeightLarge,
            )
        }
    }
}
