package com.tnyx.wear.presentation.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.tnyx.wear.presentation.components.MacronutrientRing
import com.tnyx.wear.theme.BackgroundBlack
import com.tnyx.wear.theme.TextGray
import com.tnyx.wear.theme.TextWhite
import com.tnyx.wear.theme.WearTypography

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun NutritionSummaryScreen(
    onNavigateToCalorieSummary: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    consumedCalories: Int = 1450,
    goalCalories: Int = 2000,
    carbsConsumed: Int = 180,
    carbsGoal: Int = 250,
    fatConsumed: Int = 45,
    fatGoal: Int = 65,
    proteinConsumed: Int = 90,
    proteinGoal: Int = 130,
    waterCups: Int = 6,
    waterGoalCups: Int = 8
) {
    val columnState = rememberColumnState()

    val calorieProgress = if (goalCalories > 0) consumedCalories.toFloat() / goalCalories else 0.0f
    val carbsProgress = if (carbsGoal > 0) carbsConsumed.toFloat() / carbsGoal else 0.0f
    val fatProgress = if (fatGoal > 0) fatConsumed.toFloat() / fatGoal else 0.0f
    val proteinProgress = if (proteinGoal > 0) proteinConsumed.toFloat() / proteinGoal else 0.0f
    val waterProgress = if (waterGoalCups > 0) waterCups.toFloat() / waterGoalCups else 0.0f

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundBlack)
        ) {
            // 1. Headers
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "Today",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Daily Summary",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }

            // 2. Row 1: Carbs, Fat, Protein
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
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

            // 3. Row 2: Calories (Clickable), Water
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clickable { onNavigateToCalorieSummary() }
                            .padding(horizontal = 6.dp)
                    ) {
                        MacronutrientRing(
                            label = "calories",
                            value = consumedCalories,
                            progress = calorieProgress,
                            color = Color(0xFF0056C6)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        MacronutrientRing(
                            label = "water",
                            value = waterCups,
                            progress = waterProgress,
                            color = Color(0xFF86AADA)
                        )
                    }
                }
            }

            // 4. Scrollable Calorie Detail Shortcut Pill
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Chip(
                        onClick = onNavigateToCalorieSummary,
                        label = {
                            Text(
                                text = "Calorie Details ›",
                                style = WearTypography.title1,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier
                            .width(145.dp)
                            .height(38.dp)
                    )
                }
            }
        }
    }
}
