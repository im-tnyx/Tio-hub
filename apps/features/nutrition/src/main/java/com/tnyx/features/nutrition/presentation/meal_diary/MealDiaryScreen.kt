package com.tnyx.features.nutrition.presentation.meal_diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.ui.components.calendar.TnyxWeeklyCalendar
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.presentation.meal_diary.widgets.*

@Composable
fun MealDiaryScreen(
    state: MealDiaryUiState,
    onAction: (MealDiaryAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding() // स्टेटस बार (Battery/Network) के नीचे खिसकाने के लिए
    ) {
        // --- Sticky Weekly Calendar ---
        TnyxWeeklyCalendar(
            selectedDate = state.selectedDate,
            onDateSelected = { onAction(MealDiaryAction.DateSelected(it)) }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            item {
                NutritionNutrientGrid(
                    state = state,
                    onOverviewRequested = { onAction(MealDiaryAction.OverviewRequested(it)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                NutritionVitaminSection(
                    onOverviewRequested = { onAction(MealDiaryAction.OverviewRequested(it)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.isHistoryEmpty) {
                item {
                    NutritionEmptyStateCard()
                }
            } else {
                val groupedMeals = state.meals.groupBy { it.type }
                groupedMeals.forEach { (type, meals) ->
                    item {
                        MealGroupHeader(type = type)
                    }
                    items(meals) { meal ->
                        NutritionMealCard(
                            meal = meal,
                            onTap = { onAction(MealDiaryAction.MealClicked(meal)) },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealGroupHeader(type: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = type.uppercase(),
            style = TnyxTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = TnyxTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = TnyxTheme.colors.textPrimary.copy(alpha = 0.1f)
        )
    }
}
