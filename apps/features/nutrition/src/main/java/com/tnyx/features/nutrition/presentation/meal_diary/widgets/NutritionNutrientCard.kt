package com.tnyx.features.nutrition.presentation.meal_diary.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme

@Composable
fun NutritionNutrientCard(
    label: String,
    value: String,
    goal: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color? = null,
    onTap: (() -> Unit)? = null
) {
    val cardColor = TnyxTheme.colors.surfaceRaised
    val onSurfaceColor = TnyxTheme.colors.textPrimary
    val iconColor = color ?: onSurfaceColor.copy(alpha = 0.8f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(cardColor)
            .border(
                width = 1.dp,
                color = onSurfaceColor.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(enabled = onTap != null) { onTap?.invoke() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = iconColor
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = TnyxTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = TnyxTheme.typography.bodySmall,
                    color = onSurfaceColor.copy(alpha = 0.6f)
                )
            }
        }
        
        Text(
            text = "/ $goal $unit",
            style = TnyxTheme.typography.labelSmall,
            color = onSurfaceColor.copy(alpha = 0.4f),
            fontSize = 10.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Progress Bar
        Box(
            modifier = Modifier
                .height(6.dp)
                .width(75.dp)
                .background(
                    color = onSurfaceColor.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(3.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.3f) // Dummy progress
                    .background(
                        color = color ?: onSurfaceColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = label,
            style = TnyxTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = onSurfaceColor,
            fontSize = 10.sp
        )
    }
}
