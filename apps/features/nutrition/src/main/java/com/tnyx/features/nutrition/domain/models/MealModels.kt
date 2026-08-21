package com.tnyx.features.nutrition.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class MealItem(
    val id: String,
    val name: String,
    val calories: Int, // Base calories for 1 unit
    val protein: Double, // Base protein for 1 unit
    val quantity: Double,
    val unit: String,
    val carbs: Double = 0.0,
    val fats: Double = 0.0,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val transFat: Double = 0.0,
    val saturatedFat: Double = 0.0,
    val sodium: Double? = null,
    val cholesterol: Double? = null,
    val micronutrients: MicronutrientSnapshot = MicronutrientSnapshot(),
    val servingSnapshot: ServingSnapshot? = null,
    val rawInput: String? = null,
    val inputSource: String = "manual",
    val imageUrl: String? = null,
    val confidenceScore: Double? = null,
    val nutritionSnapshot: NutritionSnapshot? = null,
) {
    val totalCalories: Int get() = (calories * quantity).toInt()
    val totalProtein: Double get() = protein * quantity
}

@Immutable
@Serializable
data class MicronutrientSnapshot(
    val vitaminAMcgRae: Double? = null,
    val vitaminCMg: Double? = null,
    val vitaminDMcg: Double? = null,
    val vitaminEMg: Double? = null,
    val vitaminKMcg: Double? = null,
    val thiaminMg: Double? = null,
    val riboflavinMg: Double? = null,
    val niacinMg: Double? = null,
    val vitaminB6Mg: Double? = null,
    val vitaminB12Mcg: Double? = null,
    val folateMcg: Double? = null,
    val calciumMg: Double? = null,
    val ironMg: Double? = null,
    val magnesiumMg: Double? = null,
    val potassiumMg: Double? = null,
    val zincMg: Double? = null,
    val seleniumMcg: Double? = null,
    val phosphorusMg: Double? = null,
    val copperMg: Double? = null,
    val manganeseMg: Double? = null,
    val iodineMcg: Double? = null,
) {
    fun values(): List<Double> = listOfNotNull(
        vitaminAMcgRae,
        vitaminCMg,
        vitaminDMcg,
        vitaminEMg,
        vitaminKMcg,
        thiaminMg,
        riboflavinMg,
        niacinMg,
        vitaminB6Mg,
        vitaminB12Mcg,
        folateMcg,
        calciumMg,
        ironMg,
        magnesiumMg,
        potassiumMg,
        zincMg,
        seleniumMcg,
        phosphorusMg,
        copperMg,
        manganeseMg,
        iodineMcg,
    )
}

@Immutable
@Serializable
data class ServingSnapshot(
    val label: String? = null,
    val amount: Double? = null,
    val unit: String? = null,
    val grams: Double? = null,
)

@Immutable
@Serializable
data class NutritionSnapshot(
    val schemaVersion: Int = 1,
    val provider: String? = null,
    val providerFoodId: String? = null,
    val brand: String? = null,
    val barcode: String? = null,
    val region: String? = null,
)

@Immutable
@Serializable
data class NutritionMeal(
    val id: String,
    val name: String,
    val type: String, // BREAKFAST, LUNCH, etc.
    val loggedAtEpochMillis: Long? = null,
    val imageUrl: String? = null,
    val items: List<MealItem> = emptyList(),
    val servingSize: Double = 1.0,
    val servingsDescription: String = "",
    val description: String = ""
) {
    val totalCalories: Int get() = items.sumOf { it.totalCalories }
    val totalProtein: Double get() = items.sumOf { it.totalProtein }
    val totalFiber: Double get() = items.sumOf { it.fiber * it.quantity }
    val totalCarbs: Double get() = items.sumOf { it.carbs * it.quantity }
    val totalFats: Double get() = items.sumOf { it.fats * it.quantity }
}
