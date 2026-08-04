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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.tnyx.wear.presentation.components.MacronutrientRing
import com.tnyx.wear.theme.BackgroundBlack
import com.tnyx.wear.theme.CardBackground
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
    val pagerState = rememberPagerState(pageCount = { 3 })

    val pageIndicatorState = object : PageIndicatorState {
        override val pageCount: Int = 3
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
                2 -> NutrientsListPage(
                    carbsConsumed = carbsConsumed.toFloat(),
                    carbsGoal = carbsGoal.toFloat(),
                    fatConsumed = fatConsumed.toFloat(),
                    fatGoal = fatGoal.toFloat(),
                    proteinConsumed = proteinConsumed.toFloat(),
                    proteinGoal = proteinGoal.toFloat()
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

@OptIn(ExperimentalHorologistApi::class)
@Composable
private fun NutrientsListPage(
    carbsConsumed: Float,
    carbsGoal: Float,
    fatConsumed: Float,
    fatGoal: Float,
    proteinConsumed: Float,
    proteinGoal: Float
) {
    val columnState = rememberColumnState()

    val nutrientsList = listOf(
        NutrientItem("Protein", proteinConsumed, proteinGoal, "g", ColorStress),
        NutrientItem("Carbohydrates", carbsConsumed, carbsGoal, "g", ColorSteps),
        NutrientItem("Fiber", 18f, 30f, "g", ColorSteps),
        NutrientItem("Sugar", 24f, 50f, "g", ColorSleep),
        NutrientItem("Fat", fatConsumed, fatGoal, "g", ColorSleep),
        NutrientItem("Saturated Fat", 12f, 20f, "g", ColorSleep),
        NutrientItem("Polyunsaturated Fat", 6f, 10f, "g", ColorSleep),
        NutrientItem("Monounsaturated Fat", 10f, 15f, "g", ColorSleep),
        NutrientItem("Trans Fat", 0f, 2f, "g", ColorSleep),
        NutrientItem("Cholesterol", 180f, 300f, "mg", ColorWater),
        NutrientItem("Sodium", 1400f, 2300f, "mg", ColorWater),
        NutrientItem("Potassium", 2200f, 3500f, "mg", ColorWater),
        NutrientItem("Vitamin A", 80f, 100f, "%", ColorWater),
        NutrientItem("Vitamin C", 90f, 100f, "%", ColorWater),
        NutrientItem("Calcium", 75f, 100f, "%", ColorWater),
        NutrientItem("Iron", 60f, 100f, "%", ColorWater)
    )

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
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "Nutrients",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Daily Intake",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }

            items(nutrientsList.size) { index ->
                val item = nutrientsList[index]
                NutrientRow(
                    name = item.name,
                    consumed = item.consumed,
                    goal = item.goal,
                    unit = item.unit,
                    color = item.color
                )
            }
        }
    }
}

@Composable
private fun NutrientRow(
    name: String,
    consumed: Float,
    goal: Float,
    unit: String,
    color: Color
) {
    val progress = if (goal > 0) consumed / goal else 0.0f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = "${consumed.toInt()}/${goal.toInt()} $unit",
                fontSize = 10.sp,
                color = TextGray
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        NutrientProgressBar(
            progress = progress,
            color = color
        )
    }
}

@Composable
private fun NutrientProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(CardBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}

private data class NutrientItem(
    val name: String,
    val consumed: Float,
    val goal: Float,
    val unit: String,
    val color: Color
)
