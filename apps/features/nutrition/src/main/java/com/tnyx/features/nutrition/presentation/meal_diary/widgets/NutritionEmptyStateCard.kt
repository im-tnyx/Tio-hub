package com.tnyx.features.nutrition.presentation.meal_diary.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tnyx.core.theme.TnyxTheme

@Composable
fun NutritionEmptyStateCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(TnyxTheme.colors.surfaceRaised)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No meals logged yet",
                style = TnyxTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TnyxTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start logging your meals to see your daily nutrient breakdown.",
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
