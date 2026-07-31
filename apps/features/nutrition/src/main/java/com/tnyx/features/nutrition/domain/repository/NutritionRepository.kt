package com.tnyx.features.nutrition.domain.repository

import com.tnyx.features.nutrition.domain.models.MealDiarySnapshot
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.models.NutritionTargetsSnapshot
import java.time.LocalDate

interface NutritionRepository {
    suspend fun getMealDiary(date: LocalDate): MealDiarySnapshot
    suspend fun getNutritionTargets(): NutritionTargetsSnapshot
    suspend fun updateNutritionTargets(targets: NutritionTargetsSnapshot)

    // Meal Log CRUD
    suspend fun saveMealLog(date: LocalDate, meal: NutritionMeal): NutritionMeal
    suspend fun deleteMealLog(mealId: String)
    suspend fun saveMealLogItem(mealLogId: String, item: MealItem): MealItem
    suspend fun updateMealLogItem(item: MealItem)
    suspend fun deleteMealLogItem(itemId: String)
}

