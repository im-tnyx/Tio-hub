package com.tnyx.features.nutrition.presentation.meal_editor.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.features.nutrition.domain.models.MealItem

@Composable
fun MealItemTile(
    item: MealItem,
    onDelete: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TnyxTheme.colors.surfaceRaised)
            .clickable { onTap() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = TnyxTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary
            )
            Text(
                text = "${item.quantity} ${item.unit} • ${item.totalCalories} kcal",
                style = TnyxTheme.typography.bodySmall,
                color = TnyxTheme.colors.textSecondary
            )
        }
        
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete Item",
                tint = TnyxTheme.colors.error.copy(alpha = 0.8f)
            )
        }
    }
}
