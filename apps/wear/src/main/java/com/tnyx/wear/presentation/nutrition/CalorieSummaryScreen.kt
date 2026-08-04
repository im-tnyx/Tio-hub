package com.tnyx.wear.presentation.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.tnyx.wear.presentation.components.MacronutrientRing
import com.tnyx.wear.theme.BackgroundBlack
import com.tnyx.wear.theme.ColorWater
import com.tnyx.wear.theme.TextGray
import com.tnyx.wear.theme.TextWhite

@Composable
fun CalorieSummaryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    consumedCalories: Int = 1450,
    goalCalories: Int = 2000,
    carbsConsumed: Int = 180,
    carbsGoal: Int = 250,
    fatConsumed: Int = 45,
    fatGoal: Int = 65,
    proteinConsumed: Int = 90,
    proteinGoal: Int = 130
) {
    val carbsProgress = if (carbsGoal > 0) carbsConsumed.toFloat() / carbsGoal else 0.0f
    val fatProgress = if (fatGoal > 0) fatConsumed.toFloat() / fatGoal else 0.0f
    val proteinProgress = if (proteinGoal > 0) proteinConsumed.toFloat() / proteinGoal else 0.0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = "Today",
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Calories",
                color = TextGray,
                fontSize = 12.sp
            )
            Text(
                text = consumedCalories.toString(),
                color = ColorWater,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = "of $goalCalories kcal",
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                MacronutrientRing(
                    label = "carbs",
                    value = carbsConsumed,
                    progress = carbsProgress,
                    color = Color(0xFF2BB9B0)
                )
                MacronutrientRing(
                    label = "fat",
                    value = fatConsumed,
                    progress = fatProgress,
                    color = Color(0xFFC576E1)
                )
                MacronutrientRing(
                    label = "protein",
                    value = proteinConsumed,
                    progress = proteinProgress,
                    color = Color(0xFFFEB13D)
                )
            }
        }
    }
}
