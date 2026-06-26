package com.tnyx.features.nutrition.presentation.meal_diary.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant

@Composable
fun NutritionNutrientCard(
    label: String,
    value: String,
    goal: String,
    unit: String,
    icon: Any, 
    modifier: Modifier = Modifier,
    color: Color? = null,
    progress: Float = 0f,
    onTap: (() -> Unit)? = null
) {
    val onSurfaceColor = TnyxTheme.colors.textPrimary
    val iconColor = color ?: onSurfaceColor.copy(alpha = 0.8f)
    val tokens = TnyxTheme.dimens

    // Vibrant Gradient based on Nutrient Color
    val barBrush = if (color != null) {
        Brush.horizontalGradient(
            colors = listOf(
                color.copy(alpha = 0.4f),
                color
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                onSurfaceColor.copy(alpha = 0.1f),
                onSurfaceColor.copy(alpha = 0.3f)
            )
        )
    }

    TnyxCard(
        modifier = modifier,
        variant = TnyxCardVariant.Normal,
        padding = tokens.SpaceS,
        onClick = onTap
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Handle both ImageVector and Painter
            when (icon) {
                is ImageVector -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(tokens.IconS),
                    tint = iconColor
                )
                is Painter -> Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(tokens.IconS),
                    tint = iconColor
                )
            }
            
            Spacer(modifier = Modifier.height(tokens.SpaceXXS))
            
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
                    Spacer(modifier = Modifier.width(tokens.SpaceXXS))
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
            
            Spacer(modifier = Modifier.height(tokens.SpaceXS))
            
            // 3. Progress Bar (Track color is now a faint version of Nutrient Color)
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .fillMaxWidth(0.9f)
                    .background(
                        color = (color ?: onSurfaceColor).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(tokens.RadiusFull)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0f, 1f)) 
                        .background(
                            brush = barBrush,
                            shape = RoundedCornerShape(tokens.RadiusFull)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(tokens.SpaceS))
            
            Text(
                text = label,
                style = TnyxTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}
