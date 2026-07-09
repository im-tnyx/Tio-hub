package com.tnyx.core.ui.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.tnyx.core.theme.TnyxTheme

/**
 * Tnyx Standard Monochrome TextField
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TnyxTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperMessage: String? = null
) {
    val tokens = TnyxTheme.components.input
    val isFocused by interactionSource.collectIsFocusedAsState()

    val textColor = when {
        !enabled -> tokens.textColor.copy(alpha = 0.38f)
        isError -> TnyxTheme.colors.error
        isFocused -> TnyxTheme.colors.textPrimary
        else -> TnyxTheme.colors.textSecondary
    }
    
    val indicatorColor = if (isError) tokens.errorIndicatorColor else tokens.focusedIndicatorColor

    val colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = tokens.containerColor,
        unfocusedContainerColor = tokens.containerColor,
        disabledContainerColor = tokens.containerColor.copy(alpha = 0.38f),
        errorContainerColor = tokens.containerColor,
        focusedBorderColor = indicatorColor,
        unfocusedBorderColor = if (isError) tokens.errorIndicatorColor else tokens.unfocusedIndicatorColor,
        disabledBorderColor = tokens.unfocusedIndicatorColor.copy(alpha = 0.38f),
        errorBorderColor = tokens.errorIndicatorColor,
        cursorColor = TnyxTheme.colors.primary,
        errorCursorColor = tokens.errorIndicatorColor,
        focusedTextColor = TnyxTheme.colors.textPrimary,
        unfocusedTextColor = TnyxTheme.colors.textPrimary,
        disabledTextColor = TnyxTheme.colors.textPrimary.copy(alpha = 0.38f),
        errorTextColor = TnyxTheme.colors.error,
        focusedPlaceholderColor = tokens.placeholderColor,
        unfocusedPlaceholderColor = tokens.placeholderColor,
        disabledPlaceholderColor = tokens.placeholderColor.copy(alpha = 0.38f),
        errorPlaceholderColor = tokens.placeholderColor,
        focusedLabelColor = TnyxTheme.colors.primary,
        unfocusedLabelColor = TnyxTheme.colors.textSecondary,
        disabledLabelColor = TnyxTheme.colors.textMuted,
        errorLabelColor = TnyxTheme.colors.error,
        focusedLeadingIconColor = TnyxTheme.colors.textSecondary,
        unfocusedLeadingIconColor = TnyxTheme.colors.textMuted,
        focusedTrailingIconColor = TnyxTheme.colors.textSecondary,
        unfocusedTrailingIconColor = TnyxTheme.colors.textMuted
    )

    Column(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (label != null) Modifier.padding(top = TnyxTheme.dimens.SpaceXXS) else Modifier)
                .heightIn(min = tokens.height),
            interactionSource = interactionSource,
            singleLine = singleLine,
            enabled = enabled,
            readOnly = readOnly,
            minLines = minLines,
            maxLines = if (singleLine) 1 else maxLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            textStyle = (tokens.textStyle ?: TnyxTheme.typography.bodyLarge).copy(color = textColor),
            cursorBrush = SolidColor(if (isError) tokens.errorIndicatorColor else TnyxTheme.colors.primary),
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    visualTransformation = visualTransformation,
                    innerTextField = innerTextField,
                    singleLine = singleLine,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    label = label,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    colors = colors,
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = TnyxTheme.shapes.Material.medium,
                            focusedBorderThickness = tokens.borderWidthFocused,
                            unfocusedBorderThickness = tokens.borderWidthUnfocused
                        )
                    }
                )
            }
        )

        val message = if (isError && !errorMessage.isNullOrBlank()) errorMessage else helperMessage
        if (!message.isNullOrBlank()) {
            Text(
                text = message,
                color = if (isError) TnyxTheme.colors.error else TnyxTheme.colors.textSecondary,
                style = TnyxTheme.typography.labelSmall,
                modifier = Modifier.padding(start = TnyxTheme.dimens.SpaceS, top = TnyxTheme.dimens.SpaceXXS)
            )
        }
    }
}

/**
 * 🔹 TnyxPhoneField
 * Standard phone input field with consistent styling.
 * This is a stateless UI component.
 */
@Composable
fun TnyxPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Mobile number",
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    helperMessage: String? = "Use +country code, or 10 digit Indian number",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    TnyxTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        enabled = enabled,
        isError = isError,
        errorMessage = errorMessage,
        helperMessage = helperMessage,
        singleLine = true
    )
}
