package com.tnyx.features.nutrition.domain.repository

import com.tnyx.features.nutrition.domain.models.MealDiarySnapshot
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.MealPhotoUpdate
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.models.NutritionTargetsSnapshot
import java.time.LocalDate

interface NutritionRepository {
    suspend fun getMealDiary(date: LocalDate): MealDiarySnapshot
    suspend fun getMealLog(mealId: String): NutritionMeal?
    suspend fun getNutritionTargets(): NutritionTargetsSnapshot
    suspend fun updateNutritionTargets(targets: NutritionTargetsSnapshot)

    // Meal log aggregate persistence. Items are written with their parent meal.
    suspend fun saveMealLog(date: LocalDate, meal: NutritionMeal): NutritionMeal
    suspend fun saveMealLogWithPhoto(
        date: LocalDate,
        meal: NutritionMeal,
        photoUpdate: MealPhotoUpdate,
    ): NutritionMeal {
        check(photoUpdate == MealPhotoUpdate.Unchanged) {
            "This nutrition repository does not support meal photos."
        }
        return saveMealLog(date, meal)
    }
    suspend fun deleteMealLog(mealId: String)
    suspend fun saveMealLogItem(mealLogId: String, item: MealItem): MealItem
    suspend fun updateMealLogItem(item: MealItem)
    suspend fun deleteMealLogItem(itemId: String)
}

