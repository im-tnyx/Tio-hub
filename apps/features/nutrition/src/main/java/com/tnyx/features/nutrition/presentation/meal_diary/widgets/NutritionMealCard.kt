package com.tnyx.features.nutrition.presentation.meal_diary.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Restaurant
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
import com.tnyx.features.nutrition.domain.models.NutritionMeal

@Composable
fun NutritionMealCard(
    meal: NutritionMeal,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(TnyxTheme.colors.surfaceRaised)
            .clickable { onTap() }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 1. Image / Icon Placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(TnyxTheme.colors.textPrimary.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Restaurant, // Meal Placeholder
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = TnyxTheme.colors.textPrimary.copy(alpha = 0.5f)
                )
            }

            // 2. Info Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 8.dp, bottom = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = meal.name,
                        style = TnyxTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TnyxTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    IconButton(onClick = { /* Handle more */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = TnyxTheme.colors.textPrimary.copy(alpha = 0.6f)
                        )
                    }
                }
                
                NutrientRow(
                    label = "Quantity",
                    value = "${String.format("%.1f", meal.servingSize)} units"
                )
                NutrientRow(
                    label = "Calories",
                    value = "${meal.totalCalories} kcal"
                )
                NutrientRow(
                    label = "Protein",
                    value = "${String.format("%.1f", meal.totalProtein)} g"
                )
            }
        }
    }
}

@Composable
private fun NutrientRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textPrimary.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = TnyxTheme.typography.bodyMedium,
            color = TnyxTheme.colors.textPrimary.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}
