package com.tnyx.features.workout.presentation.library.createexercise.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.foundation.TnyxDimens

/**
 * Option selection row built using [TnyxTheme] typography, colors, and [TnyxDimens] design tokens.
 */
@Composable
fun CreateExerciseOptionRow(
    icon: ImageVector,
    title: String,
    selectedText: String,
    hasOptionalText: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = TnyxDimens.SpaceSM, horizontal = TnyxDimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TnyxTheme.colors.textSecondary.copy(alpha = 0.7f),
                modifier = Modifier.size(TnyxDimens.IconM)
            )

            Spacer(modifier = Modifier.width(TnyxDimens.SpaceM))

            Column {
                Text(
                    text = title,
                    style = TnyxTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = TnyxTheme.colors.textPrimary,
                )
                Spacer(modifier = Modifier.height(TnyxDimens.SpaceS))
                if (hasOptionalText && (selectedText.equals("Select", ignoreCase = true) || selectedText.contains("optional", ignoreCase = true))) {
                    Row {
                        Text(
                            text = "Select ",
                            style = TnyxTheme.typography.bodyMedium,
                            color = TnyxTheme.colors.accent,
                        )
                        Text(
                            text = "(optional)",
                            style = TnyxTheme.typography.bodyMedium,
                            color = TnyxTheme.colors.textSecondary,
                        )
                    }
                } else {
                    Text(
                        text = selectedText,
                        style = TnyxTheme.typography.bodyMedium,
                        color = TnyxTheme.colors.accent,
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Select $title",
            tint = TnyxTheme.colors.textSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(TnyxDimens.IconS)
        )
    }
}
