package com.tnyx.wear.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.tnyx.wear.R
import com.tnyx.wear.presentation.components.HealthCard
import com.tnyx.wear.theme.WearTypography

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun HomeDashboardScreen(
    onNavigateToWorkout: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToAddFood: () -> Unit,
    onNavigateToAddWater: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val columnState = rememberColumnState()
    
    // Dynamic states (will sync via DataClient later)
    val routineName = "Running (5K)"
    val workoutsCount = "3 workouts"
    val dailyCalories = "Active: 450 kcal"
    val nutritionCalories = "1,200 kcal"
    
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = modifier.fillMaxSize()
        ) {
            // Header/Title
            item {
                Text(
                    text = "Tnyx Health",
                    style = WearTypography.title1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // 1. Workout Routine Card
            item {
                HealthCard(
                    icon = painterResource(id = R.drawable.ic_routine),
                    title = "Exercise",
                    valueText = "Work out",
                    onClick = onNavigateToWorkout
                )
            }
            
            // 2. This Week's Workouts Card
            item {
                HealthCard(
                    icon = painterResource(id = R.drawable.ic_workout),
                    title = "Workouts this week",
                    valueText = "3 times",
                    onClick = onNavigateToHistory
                )
            }
            
            // 3. Daily Summary Card
            item {
                HealthCard(
                    icon = painterResource(id = R.drawable.ic_summary),
                    title = "Daily Summary",
                    valueText = dailyCalories,
                    onClick = onNavigateToSummary
                )
            }
            
            // 4. Nutrition Log Card
            item {
                HealthCard(
                    icon = painterResource(id = R.drawable.ic_food),
                    title = "Nutrition Log",
                    valueText = nutritionCalories,
                    onClick = onNavigateToNutrition
                )
            }
            
            // 5. Add Food Shortcut Card
            item {
                HealthCard(
                    icon = painterResource(id = R.drawable.ic_add),
                    title = "Add Food",
                    valueText = "Log calories",
                    onClick = onNavigateToAddFood
                )
            }
            
            // 6. Add Water Shortcut Card
            item {
                HealthCard(
                    icon = painterResource(id = R.drawable.ic_water),
                    title = "Add Water",
                    valueText = "Log cups",
                    onClick = onNavigateToAddWater
                )
            }
            
            // 7. Settings Shortcut Button (TNYX Wear centered pill button)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Chip(
                        onClick = onNavigateToSettings,
                        label = {
                            Text(
                                text = "Settings",
                                style = WearTypography.title1,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier
                            .width(140.dp)
                            .height(40.dp)
                    )
                }
            }
        }
    }
}
