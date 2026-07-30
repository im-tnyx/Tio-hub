package com.tnyx.features.nutrition.domain.models

data class NutritionTargetsSnapshot(
    val dynamicCaloriesEnabled: Boolean,
    val caloriesTarget: Int,
    val calorieSurplusTarget: Int,
    val proteinTarget: Double,
    val carbsTarget: Double,
    val fatTarget: Double,
    val fiberTarget: Double,
    val waterTargetLitres: Double,
    val glassSizeMl: Int,
    val stepsTarget: Int,
    val targetWeight: Double,
    val sleepTargetHours: String,
    val formattedSleepTime: String,
    val formattedWakeTime: String,
)
