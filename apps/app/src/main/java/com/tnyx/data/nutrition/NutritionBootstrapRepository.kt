package com.tnyx.data.nutrition

import com.tnyx.features.nutrition.domain.models.MealDiarySnapshot
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.MealPhotoUpdate
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.models.NutritionTargetsSnapshot
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import io.ktor.http.ContentType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.hours

private const val DEFAULT_SLEEP_LABEL = "10:00 PM"
private const val DEFAULT_WAKE_LABEL = "06:00 AM"
private val dbTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val uiTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
private const val MAX_MEAL_PHOTO_BYTES = 10 * 1024 * 1024
private val MEAL_PHOTO_SIGNED_URL_TTL = 1.hours

class NutritionBootstrapRepository @Inject constructor(
    private val sessionProvider: AuthSessionProvider,
    private val supabaseClient: SupabaseClient,
) : NutritionRepository {

    // ── Diary ──────────────────────────────────────────────────────────────

    override suspend fun getMealDiary(date: LocalDate): MealDiarySnapshot {
        val goals = getNutritionTargets()
        val meals = fetchMealLogsForDate(date)

        return MealDiarySnapshot(
            selectedDate = date,
            hasDietPlan = goals.hasConfiguredTargets(),
            caloriesGoal = goals.caloriesTarget,
            proteinGoal = goals.proteinTarget,
            fiberGoal = goals.fiberTarget,
            carbsGoal = goals.carbsTarget,
            sugarGoal = 0.0,
            fatsGoal = goals.fatTarget,
            waterConsumedLiters = 0.0,
            waterGoalLiters = goals.waterTargetLitres,
            vitaminsProgress = 0.0,
            mineralsProgress = 0.0,
            meals = meals,
        )
    }

    private suspend fun fetchMealLogsForDate(date: LocalDate): List<NutritionMeal> {
        val userId = resolvedUserId()
            ?: return emptyList()

        val logs = supabaseClient.from("meal_logs").select {
            filter {
                eq("user_id", userId)
                eq("log_date", date.toString())
            }
        }.decodeList<MealLogDto>()

        return logs.map { log ->
            val items = supabaseClient.from("meal_log_items").select {
                filter {
                    eq("meal_log_id", log.id)
                    eq("user_id", userId)
                }
            }.decodeList<MealLogItemDto>()
            log.toNutritionMeal(
                items = items,
                resolvedImageUrl = resolveMealPhotoReference(log.image_url, userId),
            )
        }
    }

    override suspend fun getMealLog(mealId: String): NutritionMeal? {
        val userId = requireUserId()
        val log = supabaseClient.from("meal_logs").select {
            filter {
                eq("id", mealId)
                eq("user_id", userId)
            }
        }.decodeList<MealLogDto>().firstOrNull() ?: return null

        val items = supabaseClient.from("meal_log_items").select {
            filter {
                eq("meal_log_id", mealId)
                eq("user_id", userId)
            }
        }.decodeList<MealLogItemDto>()
        return log.toNutritionMeal(
            items = items,
            resolvedImageUrl = resolveMealPhotoReference(log.image_url, userId),
        )
    }

    // ── Nutrition Targets ──────────────────────────────────────────────────

    override suspend fun getNutritionTargets(): NutritionTargetsSnapshot {
        val localSession = sessionProvider.currentSession()
        val supabaseUserId = resolvedUserId()
        if (localSession == null || supabaseUserId.isNullOrBlank()) {
            return nutritionTargetsDefaults()
        }

        return runCatching {
            supabaseClient.from("user_nutrition_profiles").select {
                filter {
                    eq("user_id", supabaseUserId)
                }
            }.decodeList<UserNutritionProfileDto>().firstOrNull()
        }.map { dto ->
            dto?.toTargetsSnapshot() ?: nutritionTargetsDefaults()
        }.getOrElse {
            nutritionTargetsDefaults()
        }
    }

    override suspend fun updateNutritionTargets(targets: NutritionTargetsSnapshot) {
        val localSession = sessionProvider.currentSession()
        val supabaseUserId = resolvedUserId()
        require(localSession != null && !supabaseUserId.isNullOrBlank()) {
            "A signed-in user is required to update nutrition targets"
        }

        val sleepHours = targets.sleepTargetHours.toDoubleOrNull()?.takeIf { it > 0.0 }
        val bedTime = parseUiTime(targets.formattedSleepTime) ?: parseUiTime(DEFAULT_SLEEP_LABEL)!!
        val wakeTime = sleepHours?.let { duration ->
            bedTime.plusMinutes((duration * 60.0).roundToLong())
        } ?: parseUiTime(targets.formattedWakeTime) ?: parseUiTime(DEFAULT_WAKE_LABEL)!!

        val macroTargets = buildJsonObject {
            put("calories", JsonPrimitive(targets.caloriesTarget))
            put("protein", JsonPrimitive(targets.proteinTarget))
            put("carbs", JsonPrimitive(targets.carbsTarget))
            put("fat", JsonPrimitive(targets.fatTarget))
            put("fiber", JsonPrimitive(targets.fiberTarget))
            put("calorie_surplus", JsonPrimitive(targets.calorieSurplusTarget))
            put("dynamic_calories_enabled", JsonPrimitive(targets.dynamicCaloriesEnabled))
            put("glass_size_ml", JsonPrimitive(targets.glassSizeMl))
            sleepHours?.let { put("sleep_target_hours", JsonPrimitive(it)) }
        }

        val nutritionPayload = buildJsonObject {
            put("user_id", supabaseUserId)
            targets.stepsTarget.takeIf { it > 0 }?.let { put("steps_target", it) }
            targets.waterTargetLitres.takeIf { it > 0.0 }
                ?.times(1000.0)
                ?.toInt()
                ?.let { put("water_target_ml", it) }
            targets.targetWeight.takeIf { it > 0.0 }?.let { put("target_weight_kg", it) }
            put("bed_time", bedTime.format(dbTimeFormatter))
            put("wake_up_time", wakeTime.format(dbTimeFormatter))
            put("macro_targets", macroTargets)
        }
        supabaseClient.from("user_nutrition_profiles").upsert(nutritionPayload) {
            onConflict = "user_id"
        }
    }

    // ── Meal Log CRUD ──────────────────────────────────────────────────────

    override suspend fun saveMealLog(date: LocalDate, meal: NutritionMeal): NutritionMeal {
        return saveMealLogWithPhoto(date, meal, MealPhotoUpdate.Unchanged)
    }

    override suspend fun saveMealLogWithPhoto(
        date: LocalDate,
        meal: NutritionMeal,
        photoUpdate: MealPhotoUpdate,
    ): NutritionMeal {
        val userId = requireUserId()
        val id = meal.id.ifBlank { UUID.randomUUID().toString() }

        require(meal.name.isNotBlank()) { "Meal name is required" }
        require(meal.items.isNotEmpty()) { "At least one food item is required" }

        val previousImageReference = meal.id.takeIf(String::isNotBlank)?.let { mealId ->
            supabaseClient.from("meal_logs").select {
                filter {
                    eq("id", mealId)
                    eq("user_id", userId)
                }
            }.decodeList<MealLogDto>().firstOrNull()?.image_url
        }
        val previousObjectPath = previousImageReference
            ?.toOwnedNutritionMediaObjectPath(userId)
        var uploadedObjectPath: String? = null
        val durableImageReference = when (photoUpdate) {
            MealPhotoUpdate.Unchanged -> meal.imageUrl
                ?.toNutritionMediaObjectPath()
                ?.toNutritionMediaStorageReference()
                ?: meal.imageUrl
            MealPhotoUpdate.Remove -> null
            is MealPhotoUpdate.Replace -> {
                val mediaType = requireNotNull(SUPPORTED_MEAL_PHOTOS[photoUpdate.mimeType.lowercase()]) {
                    "Selected meal photo type is not supported."
                }
                require(photoUpdate.bytes.isNotEmpty()) { "Selected meal photo is empty." }
                require(photoUpdate.bytes.size <= MAX_MEAL_PHOTO_BYTES) {
                    "Meal photo is too large. Maximum size is 10 MB."
                }
                val objectPath = "$userId/$id/${UUID.randomUUID()}.${mediaType.extension}"
                supabaseClient.storage.from(NUTRITION_MEDIA_BUCKET).upload(
                    path = objectPath,
                    data = photoUpdate.bytes,
                ) {
                    contentType = mediaType.contentType
                }
                uploadedObjectPath = objectPath
                objectPath.toNutritionMediaStorageReference()
            }
        }

        try {
            supabaseClient.from("meal_logs").upsert(
                MealLogDto(
                    id = id,
                    user_id = userId,
                    log_date = date.toString(),
                    meal_type = meal.type,
                    name = meal.name,
                    image_url = durableImageReference,
                )
            ) { onConflict = "id" }

            val existingItemIds = supabaseClient.from("meal_log_items").select {
                filter {
                    eq("meal_log_id", id)
                    eq("user_id", userId)
                }
            }.decodeList<MealLogItemDto>().mapTo(mutableSetOf()) { it.id }

            val persistedItems = meal.items.map { item ->
                require(item.name.isNotBlank()) { "Food item name is required" }
                require(item.quantity > 0.0) { "Food item quantity must be greater than zero" }
                require(item.hasValidNutrition()) { "Food item nutrition cannot be negative" }

                val itemId = item.id.ifBlank { UUID.randomUUID().toString() }
                val persistedItem = item.copy(id = itemId)
                supabaseClient.from("meal_log_items").upsert(
                    MealLogItemDto.fromDomain(
                        mealLogId = id,
                        userId = userId,
                        item = persistedItem,
                    )
                ) { onConflict = "id" }
                existingItemIds.remove(itemId)
                persistedItem
            }

            existingItemIds.forEach { staleItemId ->
                deleteMealLogItem(staleItemId)
            }

            if (photoUpdate != MealPhotoUpdate.Unchanged) {
                previousObjectPath
                    ?.takeIf { path -> path != uploadedObjectPath }
                    ?.let { path ->
                        runCatching {
                            supabaseClient.storage.from(NUTRITION_MEDIA_BUCKET).delete(path)
                        }
                    }
            }

            return meal.copy(
                id = id,
                imageUrl = durableImageReference,
                items = persistedItems,
            )
        } catch (error: Exception) {
            uploadedObjectPath?.let { path ->
                runCatching { supabaseClient.storage.from(NUTRITION_MEDIA_BUCKET).delete(path) }
            }
            throw error
        }
    }

    override suspend fun deleteMealLog(mealId: String) {
        val userId = requireUserId()
        val imageObjectPath = supabaseClient.from("meal_logs").select {
            filter {
                eq("id", mealId)
                eq("user_id", userId)
            }
        }.decodeList<MealLogDto>().firstOrNull()?.image_url
            ?.toOwnedNutritionMediaObjectPath(userId)
        supabaseClient.from("meal_logs").delete {
            filter {
                eq("id", mealId)
                eq("user_id", userId)
            }
        }
        imageObjectPath?.let { path ->
            runCatching { supabaseClient.storage.from(NUTRITION_MEDIA_BUCKET).delete(path) }
        }
    }

    override suspend fun saveMealLogItem(mealLogId: String, item: MealItem): MealItem {
        val userId = requireUserId()
        val id = item.id.ifBlank { UUID.randomUUID().toString() }

        supabaseClient.from("meal_log_items").upsert(
            MealLogItemDto.fromDomain(
                mealLogId = mealLogId,
                userId = userId,
                item = item.copy(id = id),
            )
        ) { onConflict = "id" }

        return item.copy(id = id)
    }

    override suspend fun updateMealLogItem(item: MealItem) {
        val userId = requireUserId()
        supabaseClient.from("meal_log_items").update(
            mapOf(
                "name" to item.name,
                "quantity" to item.quantity,
                "unit" to item.unit,
                "calories" to item.calories,
                "protein" to item.protein,
                "carbs" to item.carbs,
                "fats" to item.fats,
                "fiber" to item.fiber,
                "sugar" to item.sugar,
                "trans_fat" to item.transFat,
                "saturated_fat" to item.saturatedFat,
            )
        ) {
            filter {
                eq("id", item.id)
                eq("user_id", userId)
            }
        }
    }

    override suspend fun deleteMealLogItem(itemId: String) {
        val userId = requireUserId()
        supabaseClient.from("meal_log_items").delete {
            filter {
                eq("id", itemId)
                eq("user_id", userId)
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun requireUserId(): String {
        return supabaseClient.auth.currentUserOrNull()?.id
            ?: error("A signed-in Supabase user is required for meal log operations")
    }

    private fun resolvedUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    private suspend fun resolveMealPhotoReference(reference: String?, userId: String): String? {
        val value = reference ?: return null
        val objectPath = value.toOwnedNutritionMediaObjectPath(userId) ?: return value
        return runCatching {
            supabaseClient.storage.from(NUTRITION_MEDIA_BUCKET).createSignedUrl(
                path = objectPath,
                expiresIn = MEAL_PHOTO_SIGNED_URL_TTL,
            )
        }.getOrNull()
    }

    private fun UserNutritionProfileDto.toTargetsSnapshot(): NutritionTargetsSnapshot {
        val bedTime = parseDbTime(bed_time) ?: parseUiTime(DEFAULT_SLEEP_LABEL)!!
        val wakeTime = parseDbTime(wake_up_time) ?: parseUiTime(DEFAULT_WAKE_LABEL)!!
        val sleepHours = macro_targets?.sleep_target_hours
            ?.takeIf { it > 0.0 }
            ?.toCleanString()
            ?: hoursBetween(bedTime, wakeTime).toCleanString()

        return NutritionTargetsSnapshot(
            dynamicCaloriesEnabled = macro_targets?.dynamic_calories_enabled ?: false,
            caloriesTarget = macro_targets?.calories?.takeIf { it > 0 } ?: 0,
            calorieSurplusTarget = macro_targets?.calorie_surplus?.takeIf { it > 0 } ?: 0,
            proteinTarget = macro_targets?.protein?.takeIf { it > 0.0 } ?: 0.0,
            carbsTarget = macro_targets?.carbs?.takeIf { it > 0.0 } ?: 0.0,
            fatTarget = macro_targets?.fat?.takeIf { it > 0.0 } ?: 0.0,
            fiberTarget = macro_targets?.fiber?.takeIf { it > 0.0 } ?: 0.0,
            waterTargetLitres = water_target_ml?.takeIf { it > 0.0 }?.div(1000.0) ?: 0.0,
            glassSizeMl = macro_targets?.glass_size_ml?.takeIf { it > 0 } ?: 250,
            stepsTarget = steps_target?.takeIf { it > 0 } ?: 0,
            targetWeight = target_weight_kg?.takeIf { it > 0.0 } ?: 0.0,
            sleepTargetHours = sleepHours,
            formattedSleepTime = bedTime.format(uiTimeFormatter),
            formattedWakeTime = wakeTime.format(uiTimeFormatter),
        )
    }

    private fun parseDbTime(value: String?): LocalTime? {
        val normalized = value?.trim().orEmpty()
        if (normalized.isBlank()) return null
        return runCatching { LocalTime.parse(normalized, dbTimeFormatter) }.getOrNull()
            ?: runCatching { LocalTime.parse(normalized) }.getOrNull()
    }

    private fun parseUiTime(value: String): LocalTime? {
        val normalized = value.trim()
        if (normalized.isBlank()) return null
        return runCatching { LocalTime.parse(normalized, uiTimeFormatter) }.getOrNull()
    }

    private fun hoursBetween(start: LocalTime, end: LocalTime): Double {
        val startMinutes = start.hour * 60 + start.minute
        var endMinutes = end.hour * 60 + end.minute
        if (endMinutes <= startMinutes) {
            endMinutes += 24 * 60
        }
        return (endMinutes - startMinutes) / 60.0
    }

    private fun Double.toCleanString(): String {
        return if (this % 1.0 == 0.0) toInt().toString() else String.format(Locale.US, "%.1f", this)
    }
}

// ── Top-level helpers ──────────────────────────────────────────────────────

private fun NutritionTargetsSnapshot.hasConfiguredTargets(): Boolean {
    return caloriesTarget > 0 ||
        proteinTarget > 0.0 ||
        carbsTarget > 0.0 ||
        fatTarget > 0.0 ||
        fiberTarget > 0.0 ||
        waterTargetLitres > 0.0
}

private fun nutritionTargetsDefaults(): NutritionTargetsSnapshot {
    return NutritionTargetsSnapshot(
        dynamicCaloriesEnabled = false,
        caloriesTarget = 0,
        calorieSurplusTarget = 0,
        proteinTarget = 0.0,
        carbsTarget = 0.0,
        fatTarget = 0.0,
        fiberTarget = 0.0,
        waterTargetLitres = 0.0,
        glassSizeMl = 250,
        stepsTarget = 0,
        targetWeight = 0.0,
        sleepTargetHours = "8",
        formattedSleepTime = DEFAULT_SLEEP_LABEL,
        formattedWakeTime = DEFAULT_WAKE_LABEL,
    )
}

// ── DTOs ───────────────────────────────────────────────────────────────────

@Serializable
private data class UserNutritionProfileDto(
    val water_target_ml: Double? = null,
    val preferred_diet: String? = null,
    val macro_targets: MacroTargetsDto? = null,
    val steps_target: Int? = null,
    val target_weight_kg: Double? = null,
    val bed_time: String? = null,
    val wake_up_time: String? = null,
)

@Serializable
private data class MacroTargetsDto(
    val calories: Int? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val calorie_surplus: Int? = null,
    val dynamic_calories_enabled: Boolean? = null,
    val glass_size_ml: Int? = null,
    val sleep_target_hours: Double? = null,
)

@Serializable
private data class MealLogDto(
    val id: String,
    val user_id: String,
    val log_date: String,
    val meal_type: String,
    val name: String,
    val image_url: String? = null,
) {
    fun toNutritionMeal(
        items: List<MealLogItemDto>,
        resolvedImageUrl: String? = image_url,
    ): NutritionMeal = NutritionMeal(
        id = id,
        name = name,
        type = meal_type,
        imageUrl = resolvedImageUrl,
        items = items.map { it.toMealItem() },
    )
}

@Serializable
private data class MealLogItemDto(
    val id: String,
    val meal_log_id: String,
    val user_id: String,
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "serving",
    val calories: Int = 0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fats: Double = 0.0,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val trans_fat: Double = 0.0,
    val saturated_fat: Double = 0.0,
) {
    fun toMealItem(): MealItem = MealItem(
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
        transFat = trans_fat,
        saturatedFat = saturated_fat,
    )

    companion object {
        fun fromDomain(
            mealLogId: String,
            userId: String,
            item: MealItem,
        ): MealLogItemDto = MealLogItemDto(
            id = item.id,
            meal_log_id = mealLogId,
            user_id = userId,
            name = item.name.trim(),
            quantity = item.quantity,
            unit = item.unit.trim().ifBlank { "serving" },
            calories = item.calories,
            protein = item.protein,
            carbs = item.carbs,
            fats = item.fats,
            fiber = item.fiber,
            sugar = item.sugar,
            trans_fat = item.transFat,
            saturated_fat = item.saturatedFat,
        )
    }
}

private fun MealItem.hasValidNutrition(): Boolean {
    return calories >= 0 &&
        protein >= 0.0 &&
        carbs >= 0.0 &&
        fats >= 0.0 &&
        fiber >= 0.0 &&
        sugar >= 0.0 &&
        transFat >= 0.0 &&
        saturatedFat >= 0.0
}

private data class SupportedMealPhoto(
    val extension: String,
    val contentType: ContentType,
)

private val SUPPORTED_MEAL_PHOTOS = mapOf(
    "image/jpeg" to SupportedMealPhoto("jpg", ContentType.Image.JPEG),
    "image/jpg" to SupportedMealPhoto("jpg", ContentType.Image.JPEG),
    "image/png" to SupportedMealPhoto("png", ContentType.Image.PNG),
    "image/webp" to SupportedMealPhoto("webp", ContentType.parse("image/webp")),
)
