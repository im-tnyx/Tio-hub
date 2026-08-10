package com.tnyx.features.nutrition.presentation.meal_item_editor.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryVariant
import com.tnyx.core.ui.components.inputs.TnyxTextField
import com.tnyx.core.ui.components.inputs.TnyxTextFieldVariant
import com.tnyx.core.ui.components.sheets.TnyxModalBottomSheet

@Composable
fun MealItemNameBottomSheet(
    visible: Boolean,
    name: String,
    errorMessage: String?,
    onNameChanged: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!visible) return
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    TnyxModalBottomSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
        title = null,
        showDivider = false,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Edit Ingredient Name",
                modifier = Modifier.fillMaxWidth(),
                style = TnyxTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TnyxTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(TnyxTheme.dimens.SpaceL))
            TnyxTextField(
                value = name,
                onValueChange = onNameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                variant = TnyxTextFieldVariant.Default,
                placeholder = { Text("Ingredient name") },
                isError = errorMessage != null,
                errorMessage = errorMessage,
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
                    variant = TnyxSecondaryVariant.Standard,
                    height = 56.dp,
                )
                TnyxPrimaryButton(
                    text = "Save",
                    onPressed = onConfirm,
                    modifier = Modifier.weight(1f),
                    height = 56.dp,
                )
            }
        }
    }
}
