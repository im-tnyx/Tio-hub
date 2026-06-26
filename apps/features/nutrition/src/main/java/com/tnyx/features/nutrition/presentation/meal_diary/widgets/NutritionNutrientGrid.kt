package com.tnyx.features.nutrition.presentation.meal_diary.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tnyx.features.nutrition.presentation.meal_diary.MealDiaryUiState

@Composable
fun NutritionNutrientGrid(
    state: MealDiaryUiState,
    onOverviewRequested: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Row 1: 3 Items
        Row(modifier = Modifier.fillMaxWidth()) {
            NutritionNutrientCard(
                label = "Calories",
                value = "${state.caloriesConsumed}",
                goal = "${state.caloriesGoal}",
                unit = "",
                icon = Icons.Outlined.LocalFireDepartment,
                onTap = { onOverviewRequested("calories") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            NutritionNutrientCard(
                label = "Protein",
                value = "${state.proteinConsumed.toInt()}g",
                goal = "${state.proteinGoal.toInt()}g",
                unit = "",
                icon = Icons.Outlined.Egg,
                onTap = { onOverviewRequested("protein") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            NutritionNutrientCard(
                label = "Fiber",
                value = "${state.fiberConsumed.toInt()}g",
                goal = "${state.fiberGoal.toInt()}g",
                unit = "",
                icon = Icons.Outlined.Grass,
                onTap = { onOverviewRequested("fiber") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Row 2: 4 Items
        Row(modifier = Modifier.fillMaxWidth()) {
            NutritionNutrientCard(
                label = "Carbs",
                value = if (state.carbsConsumed == 0.0) "- g" else "${state.carbsConsumed.toInt()} g",
                goal = "${state.carbsGoal.toInt()}g",
                unit = "",
                icon = Icons.Outlined.Grain,
                onTap = { onOverviewRequested("carbs") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            NutritionNutrientCard(
                label = "Sugar",
                value = if (state.sugarConsumed == 0.0) "- g" else "${state.sugarConsumed.toInt()} g",
                goal = "${state.sugarGoal.toInt()}g",
                unit = "",
                icon = Icons.Outlined.Cookie,
                onTap = { onOverviewRequested("sugar") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            NutritionNutrientCard(
                label = "Fats",
                value = if (state.fatsConsumed == 0.0) "- g" else "${state.fatsConsumed.toInt()} g",
                goal = "${state.fatsGoal.toInt()}g",
                unit = "",
                icon = Icons.Outlined.WaterDrop,
                onTap = { onOverviewRequested("fats") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            NutritionNutrientCard(
                label = "Water",
                value = if (state.waterConsumed == 0.0) "- L" else "${state.waterConsumed} L",
                goal = "${state.waterGoal} L",
                unit = "",
                icon = Icons.Outlined.Opacity,
                onTap = { onOverviewRequested("water") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
