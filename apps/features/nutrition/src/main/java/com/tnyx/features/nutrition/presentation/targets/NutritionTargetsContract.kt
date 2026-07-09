package com.tnyx.features.nutrition.presentation.targets

import androidx.compose.runtime.Immutable

@Immutable
data class NutritionTargetsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val dynamicCaloriesEnabled: Boolean = true,
    val caloriesTarget: Int = 2620,
    val calorieSurplusTarget: Int = 300,
    val proteinTarget: Double = 140.0,
    val carbsTarget: Double = 260.0,
    val fatTarget: Double = 78.0,
    val fiberTarget: Double = 30.0,
    val waterTargetLitres: Double = 3.5,
    val glassSizeMl: Int = 250,
    val lastUpdatedLabel: String = "Updated today",
    val activeEditField: NutritionTargetField? = null,
    val editValue: String = "",
    val stepsTarget: Int = 10000,
    val targetWeight: Double = 70.0,
    val sleepTargetHours: String = "8.0",
    val formattedSleepTime: String = "10:00 PM",
    val formattedWakeTime: String = "06:00 AM",
)

// यहाँ से हमने एक्स्ट्रा 'object' वाली लाइनें हटा दी हैं
enum class NutritionTargetField(
    val title: String,
    val unit: String
) {
    Calories("Calories", "kcal"),
    CalorieSurplus("Calorie Surplus", "kcal"),
    Protein("Protein", "g"),
    Carbs("Carbs", "g"),
    Fat("Fat", "g"),
    Fiber("Fiber", "g"),
    Water("Water", "litres"),
    GlassSize("Glass Size", "ml"),
    Steps("Steps", "steps"),
    TargetWeight("Target Weight", "kg"),
    SleepSchedule("Sleep Schedule", "hrs") // आखिरी वैल्यू के बाद कोई कॉमा (,) नहीं आएगा
}

sealed interface NutritionTargetsAction {
    data object BackClicked : NutritionTargetsAction
    data class DynamicCaloriesChanged(val enabled: Boolean) : NutritionTargetsAction
    data class EditTargetClicked(val field: NutritionTargetField) : NutritionTargetsAction
    data class EditValueChanged(val value: String) : NutritionTargetsAction
    data object EditDismissed : NutritionTargetsAction
    data object EditSaved : NutritionTargetsAction
    data object RecalculateClicked : NutritionTargetsAction
}

sealed interface NutritionTargetsEffect {
    data object NavigateBack : NutritionTargetsEffect
    data class ShowMessage(val message: String) : NutritionTargetsEffect
}