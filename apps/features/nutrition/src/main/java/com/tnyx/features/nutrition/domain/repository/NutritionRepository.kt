package com.tnyx.features.nutrition.domain.repository

import com.tnyx.features.nutrition.domain.models.MealDiarySnapshot
import com.tnyx.features.nutrition.domain.models.NutritionTargetsSnapshot
import java.time.LocalDate

interface NutritionRepository {
    suspend fun getMealDiary(date: LocalDate): MealDiarySnapshot
    suspend fun getNutritionTargets(): NutritionTargetsSnapshot
    suspend fun updateNutritionTargets(targets: NutritionTargetsSnapshot)
}
