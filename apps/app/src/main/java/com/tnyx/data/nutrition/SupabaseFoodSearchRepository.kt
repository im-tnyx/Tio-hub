package com.tnyx.data.nutrition

import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.MicronutrientSnapshot
import com.tnyx.features.nutrition.domain.models.NutritionSnapshot
import com.tnyx.features.nutrition.domain.models.ServingSnapshot
import com.tnyx.features.nutrition.domain.repository.FoodSearchRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseFoodSearchRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
) : FoodSearchRepository {

    override suspend fun search(query: String): List<MealItem> {
        val normalizedQuery = query.trim()
        val response = invokeSearch(
            buildJsonObject { put("query", normalizedQuery) },
        )
        return response.items.map { item ->
            item.toMealItem(
                provider = response.source,
                rawInput = normalizedQuery,
                inputSource = "search",
                region = response.region,
            )
        }
    }

    override suspend fun lookupBarcode(barcode: String): MealItem? {
        val normalizedBarcode = barcode.trim()
        val response = invokeSearch(
            buildJsonObject {
                put("barcode", normalizedBarcode)
                // Keeps Android compatible with the currently deployed text-only function.
                put("query", normalizedBarcode)
            },
        )
        return response.items.firstOrNull()
            ?.takeIf { response.lookup == BARCODE_LOOKUP }
            ?.toMealItem(
                provider = response.source,
                rawInput = normalizedBarcode,
                inputSource = "barcode",
                barcode = normalizedBarcode,
                region = response.region,
            )
    }

    private suspend fun invokeSearch(body: kotlinx.serialization.json.JsonObject): FoodSearchResponseDto {
        val response = supabaseClient.functions.invoke(
            function = FUNCTION_NAME,
            body = body,
            headers = Headers.build {
                append(HttpHeaders.ContentType, "application/json")
            },
        )
        check(response.status.isSuccess()) { "Food search request failed." }
        return response.body()
    }

    private companion object {
        const val FUNCTION_NAME = "nutrition-food-search"
        const val BARCODE_LOOKUP = "barcode"
    }
}

@Serializable
private data class FoodSearchResponseDto(
    val items: List<FoodSearchItemDto> = emptyList(),
    val lookup: String? = null,
    val source: String? = null,
    val region: String? = null,
)

@Serializable
private data class FoodSearchItemDto(
    val id: String,
    val name: String,
    val calories: Int,
    val protein: Double,
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
    val providerFoodId: String? = null,
    val brand: String? = null,
) {
    fun toMealItem(
        provider: String?,
        rawInput: String,
        inputSource: String,
        barcode: String? = null,
        region: String? = null,
    ) = MealItem(
        id = id,
        name = name,
        calories = calories,
        protein = protein,
        quantity = quantity,
        unit = unit,
        carbs = carbs,
        fats = fats,
        fiber = fiber,
        sugar = sugar,
        transFat = transFat,
        saturatedFat = saturatedFat,
        sodium = sodium,
        cholesterol = cholesterol,
        micronutrients = micronutrients,
        servingSnapshot = ServingSnapshot(
            label = unit,
            amount = quantity,
            unit = unit,
            grams = unit.toGramAmountOrNull(),
        ),
        rawInput = rawInput,
        inputSource = inputSource,
        nutritionSnapshot = NutritionSnapshot(
            provider = provider,
            providerFoodId = providerFoodId,
            brand = brand,
            barcode = barcode,
            region = region,
        ),
    )
}

private fun String.toGramAmountOrNull(): Double? {
    val match = GRAM_UNIT_PATTERN.find(trim()) ?: return null
    return match.groupValues[1].toDoubleOrNull()
}

private val GRAM_UNIT_PATTERN = Regex("""\b(\d+(?:\.\d+)?)\s*g\b""", RegexOption.IGNORE_CASE)
