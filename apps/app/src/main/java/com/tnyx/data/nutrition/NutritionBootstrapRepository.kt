package com.tnyx.data.nutrition

import com.tnyx.features.nutrition.domain.models.MealDiarySnapshot
import com.tnyx.features.nutrition.domain.models.MealItem
import com.tnyx.features.nutrition.domain.models.NutritionMeal
import com.tnyx.features.nutrition.domain.models.NutritionTargetsSnapshot
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
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

private const val DEFAULT_SLEEP_LABEL = "10:00 PM"
private const val DEFAULT_WAKE_LABEL = "06:00 AM"
private val dbTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val uiTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

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
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: return emptyList()

        return runCatching {
            val logs = supabaseClient.from("meal_logs").select {
                filter {
                    eq("user_id", userId)
                    eq("log_date", date.toString())
                }
            }.decodeList<MealLogDto>()

            logs.map { log ->
                val items = supabaseClient.from("meal_log_items").select {
                    filter { eq("meal_log_id", log.id) }
                }.decodeList<MealLogItemDto>()
                log.toNutritionMeal(items)
            }
        }.getOrElse { emptyList() }
    }

    // ── Nutrition Targets ──────────────────────────────────────────────────

    override suspend fun getNutritionTargets(): NutritionTargetsSnapshot {
        val localSession = sessionProvider.currentSession()
        val supabaseUserId = supabaseClient.auth.currentUserOrNull()?.id
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
        val supabaseUserId = supabaseClient.auth.currentUserOrNull()?.id
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
        val userId = requireUserId()
        val id = meal.id.ifBlank { UUID.randomUUID().toString() }

        supabaseClient.from("meal_logs").upsert(
            MealLogDto(
                id = id,
                user_id = userId,
                log_date = date.toString(),
                meal_type = meal.type,
                name = meal.name,
                image_url = meal.imageUrl,
            )
        ) { onConflict = "id" }

        return meal.copy(id = id)
    }

    override suspend fun deleteMealLog(mealId: String) {
        val userId = requireUserId()
        supabaseClient.from("meal_logs").delete {
            filter {
                eq("id", mealId)
                eq("user_id", userId)
            }
        }
    }

    override suspend fun saveMealLogItem(mealLogId: String, item: MealItem): MealItem {
        val userId = requireUserId()
        val id = item.id.ifBlank { UUID.randomUUID().toString() }

        supabaseClient.from("meal_log_items").upsert(
            MealLogItemDto(
                id = id,
                meal_log_id = mealLogId,
                user_id = userId,
                name = item.name,
                quantity = item.quantity,
                unit = item.unit,
                calories = item.calories,
                protein = item.protein,
                carbs = item.carbs,
                fats = item.fats,
                fiber = item.fiber,
                sugar = item.sugar,
                trans_fat = item.transFat,
                saturated_fat = item.saturatedFat,
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
    fun toNutritionMeal(items: List<MealLogItemDto>): NutritionMeal = NutritionMeal(
        id = id,
        name = name,
        type = meal_type,
        imageUrl = image_url,
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
}
