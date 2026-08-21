package com.tnyx.features.nutrition.presentation.meal_editor.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.nutrition.domain.models.MealItem

@Composable
fun MealItemTile(
    item: MealItem,
    onDelete: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(
                horizontal = TnyxTheme.dimens.SpaceM,
                vertical = TnyxTheme.dimens.SpaceSM,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = TnyxTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TnyxTheme.colors.textPrimary,
            )
            Text(
                text = "${item.totalCalories} kcal  |  ${item.quantity} ${item.unit}",
                style = TnyxTheme.typography.bodySmall,
                color = TnyxTheme.colors.textSecondary,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(TnyxTheme.dimens.SpaceXXS)) {
            IconButton(onClick = onTap) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit ${item.name}",
                    tint = TnyxTheme.colors.textSecondary,
                    modifier = Modifier.size(TnyxTheme.dimens.IconS),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete ${item.name}",
                    tint = TnyxTheme.colors.error,
                    modifier = Modifier.size(TnyxTheme.dimens.IconS),
                )
            }
        }
    }
}
