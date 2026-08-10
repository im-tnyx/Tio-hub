package com.tnyx.data.nutrition

import com.tnyx.features.nutrition.domain.models.MealItem
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
        return invokeSearch(
            buildJsonObject { put("query", query.trim()) },
        ).items.map(FoodSearchItemDto::toMealItem)
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
            ?.toMealItem()
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
) {
    fun toMealItem() = MealItem(
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
    )
}
