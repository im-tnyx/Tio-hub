package com.tnyx.wear.presentation.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.tnyx.wear.presentation.components.MacronutrientRing
import com.tnyx.wear.theme.BackgroundBlack
import com.tnyx.wear.theme.ColorSleep
import com.tnyx.wear.theme.ColorSteps
import com.tnyx.wear.theme.ColorStress
import com.tnyx.wear.theme.ColorWater
import com.tnyx.wear.theme.TextGray
import com.tnyx.wear.theme.TextWhite

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun NutritionSummaryScreen(
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
    val pagerState = rememberPagerState(pageCount = { 2 })

    val pageIndicatorState = object : PageIndicatorState {
        override val pageCount: Int = 2
        override val pageOffset: Float = 0f
        override val selectedPage: Int
            get() = pagerState.currentPage
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> DailySummaryPage(
                    consumedCalories = consumedCalories,
                    goalCalories = goalCalories,
                    carbsConsumed = carbsConsumed,
                    carbsGoal = carbsGoal,
                    fatConsumed = fatConsumed,
                    fatGoal = fatGoal,
                    proteinConsumed = proteinConsumed,
                    proteinGoal = proteinGoal,
                    waterCups = waterCups,
                    waterGoalCups = waterGoalCups
                )
                1 -> CalorieDetailPage(
                    consumedCalories = consumedCalories,
                    goalCalories = goalCalories,
                    carbsConsumed = carbsConsumed,
                    carbsGoal = carbsGoal,
                    fatConsumed = fatConsumed,
                    fatGoal = fatGoal,
                    proteinConsumed = proteinConsumed,
                    proteinGoal = proteinGoal
                )
            }
        }

        HorizontalPageIndicator(
            pageIndicatorState = pageIndicatorState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        )
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
private fun DailySummaryPage(
    consumedCalories: Int,
    goalCalories: Int,
    carbsConsumed: Int,
    carbsGoal: Int,
    fatConsumed: Int,
    fatGoal: Int,
    proteinConsumed: Int,
    proteinGoal: Int,
    waterCups: Int,
    waterGoalCups: Int
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
            modifier = Modifier
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
                        color = ColorSteps
                    )
                    MacronutrientRing(
                        label = "fat",
                        value = fatConsumed,
                        progress = fatProgress,
                        color = ColorSleep
                    )
                    MacronutrientRing(
                        label = "protein",
                        value = proteinConsumed,
                        progress = proteinProgress,
                        color = ColorStress
                    )
                }
            }

            // 3. Row 2: Calories, Water
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        MacronutrientRing(
                            label = "calories",
                            value = consumedCalories,
                            progress = calorieProgress,
                            color = ColorWater
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
                            color = ColorWater
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
private fun CalorieDetailPage(
    consumedCalories: Int,
    goalCalories: Int,
    carbsConsumed: Int,
    carbsGoal: Int,
    fatConsumed: Int,
    fatGoal: Int,
    proteinConsumed: Int,
    proteinGoal: Int
) {
    val columnState = rememberColumnState()

    val carbsProgress = if (carbsGoal > 0) carbsConsumed.toFloat() / carbsGoal else 0.0f
    val fatProgress = if (fatGoal > 0) fatConsumed.toFloat() / fatGoal else 0.0f
    val proteinProgress = if (proteinGoal > 0) proteinConsumed.toFloat() / proteinGoal else 0.0f

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBlack)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 2.dp)
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
                }
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = consumedCalories.toString(),
                        color = ColorWater,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "of $goalCalories kcal",
                        color = TextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    MacronutrientRing(
                        label = "carbs",
                        value = carbsConsumed,
                        progress = carbsProgress,
                        color = ColorSteps
                    )
                    MacronutrientRing(
                        label = "fat",
                        value = fatConsumed,
                        progress = fatProgress,
                        color = ColorSleep
                    )
                    MacronutrientRing(
                        label = "protein",
                        value = proteinConsumed,
                        progress = proteinProgress,
                        color = ColorStress
                    )
                }
            }
        }
    }
}
