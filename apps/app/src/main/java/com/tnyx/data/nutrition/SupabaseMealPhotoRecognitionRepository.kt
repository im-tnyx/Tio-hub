package com.tnyx.data.nutrition

import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.MealPhotoAnalysis
import com.tnyx.features.nutrition.domain.repository.MealPhotoAnalysisException
import com.tnyx.features.nutrition.domain.repository.MealPhotoRecognitionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import java.util.Base64
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseMealPhotoRecognitionRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
) : MealPhotoRecognitionRepository {

    override suspend fun analyze(
        imageBytes: ByteArray,
        mimeType: String,
    ): MealPhotoAnalysis {
        val response = try {
            supabaseClient.functions.invoke(
                function = FUNCTION_NAME,
                body = buildJsonObject {
                    put("imageBase64", Base64.getEncoder().encodeToString(imageBytes))
                    put("mimeType", mimeType)
                },
                headers = Headers.build {
                    append(HttpHeaders.ContentType, "application/json")
                },
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw error.toSafeMealPhotoException()
        }
        if (!response.status.isSuccess()) {
            val errorMessage = runCatching {
                response.body<MealPhotoAnalysisErrorDto>().error
            }.getOrNull()?.takeIf(String::isNotBlank)
            throw MealPhotoAnalysisException(errorMessage ?: GENERIC_ANALYSIS_ERROR)
        }
        val payload = response.body<MealPhotoAnalysisResponseDto>()
        return MealPhotoAnalysis(
            suggestedName = payload.suggestedName,
            items = payload.items.map(MealPhotoItemDto::toMealItem),
        )
    }

    private companion object {
        const val FUNCTION_NAME = "nutrition-meal-photo-analyze"
        const val GENERIC_ANALYSIS_ERROR = "Meal photo analysis is unavailable. Try again later."
    }
}

private fun Exception.toSafeMealPhotoException(): MealPhotoAnalysisException {
    val responseBody = message.orEmpty().substringBefore("\nURL:").trim()
    val safeMessage = runCatching {
        Json.decodeFromString<MealPhotoAnalysisErrorDto>(responseBody).error
    }.getOrNull()?.takeIf(String::isNotBlank)
    return MealPhotoAnalysisException(
        safeMessage ?: "Meal photo analysis is unavailable. Try again later."
    )
}

@Serializable
private data class MealPhotoAnalysisErrorDto(
    val error: String = "",
)

@Serializable
private data class MealPhotoAnalysisResponseDto(
    val suggestedName: String = "Photo meal",
    val items: List<MealPhotoItemDto> = emptyList(),
)

@Serializable
private data class MealPhotoItemDto(
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
        saturatedFat = saturatedFat,
    )
}
