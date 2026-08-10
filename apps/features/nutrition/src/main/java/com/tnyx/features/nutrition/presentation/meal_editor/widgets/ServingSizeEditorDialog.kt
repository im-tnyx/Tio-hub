package com.tnyx.features.nutrition.presentation.meal_editor.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.Dialog
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxButtonSize
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryVariant
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.core.ui.components.inputs.TnyxTextFieldVariant

@Composable
fun ServingSizeEditorDialog(
    visible: Boolean,
    amount: String,
    selectedUnit: String,
    unitOptions: List<String>,
    errorMessage: String?,
    onAmountChanged: (String) -> Unit,
    onUnitSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!visible) return

    Dialog(onDismissRequest = onDismissRequest) {
        TnyxCard(
            modifier = Modifier.fillMaxWidth(),
            variant = TnyxCardVariant.Elevated,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Serving size",
                    style = TnyxTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TnyxTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))

                TnyxTextField(
                    value = amount,
                    onValueChange = onAmountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    variant = TnyxTextFieldVariant.Compact,
                    label = { Text("How much") },
                    placeholder = { Text("100") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    errorMessage = errorMessage,
                )

                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
                ServingUnitDropdown(
                    selectedUnit = selectedUnit,
                    unitOptions = unitOptions,
                    onUnitSelected = onUnitSelected,
                )

                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
                ) {
                    TnyxSecondaryButton(
                        text = "Cancel",
                        onPressed = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        variant = TnyxSecondaryVariant.Muted,
                        size = TnyxButtonSize.Compact,
                    )
                    TnyxPrimaryButton(
                        text = "Apply",
                        onPressed = onConfirm,
                        modifier = Modifier.weight(1f),
                        size = TnyxButtonSize.Compact,
                    )
                }
            }
        }
    }
}

@Composable
fun ServingCountEditorDialog(
    visible: Boolean,
    count: String,
    errorMessage: String?,
    onCountChanged: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!visible) return

    Dialog(onDismissRequest = onDismissRequest) {
        TnyxCard(
            modifier = Modifier.fillMaxWidth(),
            variant = TnyxCardVariant.Elevated,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Servings",
                    style = TnyxTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TnyxTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceM))
                TnyxTextField(
                    value = count,
                    onValueChange = onCountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    variant = TnyxTextFieldVariant.Compact,
                    label = { Text("How many servings") },
                    placeholder = { Text("1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    errorMessage = errorMessage,
                )
                Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
                DialogActions(
                    onDismissRequest = onDismissRequest,
                    onConfirm = onConfirm,
                )
            }
        }
    }
}

@Composable
private fun ServingUnitDropdown(
    selectedUnit: String,
    unitOptions: List<String>,
    onUnitSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Serving of",
            style = TnyxTheme.typography.labelMedium,
            color = TnyxTheme.colors.textSecondary,
            modifier = Modifier.padding(start = TnyxTheme.dimens.SpaceS),
        )
        Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceXXS))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val menuWidth = maxWidth
            TnyxCard(
                modifier = Modifier.fillMaxWidth(),
                variant = TnyxCardVariant.Outlined,
                padding = TnyxTheme.dimens.SpaceSM,
                onClick = { expanded = true },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selectedUnit,
                        style = TnyxTheme.typography.bodyLarge,
                        color = TnyxTheme.colors.textPrimary,
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = "Select serving unit",
                        tint = TnyxTheme.colors.textSecondary,
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(menuWidth),
            ) {
                unitOptions.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit) },
                        onClick = {
                            onUnitSelected(unit)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogActions(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceS),
    ) {
        TnyxSecondaryButton(
            text = "Cancel",
            onPressed = onDismissRequest,
            modifier = Modifier.weight(1f),
            variant = TnyxSecondaryVariant.Muted,
            size = TnyxButtonSize.Compact,
        )
        TnyxPrimaryButton(
            text = "Apply",
            onPressed = onConfirm,
            modifier = Modifier.weight(1f),
            size = TnyxButtonSize.Compact,
        )
    }
}
