package com.tnyx.data.onboarding

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import com.tnyx.features.onboarding.domain.model.OnboardingDraft
import com.tnyx.features.onboarding.domain.repository.OnboardingCompletionSyncRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Singleton
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

@Singleton
class SupabaseOnboardingCompletionSyncRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
) : OnboardingCompletionSyncRepository {

    override suspend fun syncCompletedOnboarding(draft: OnboardingDraft) {
        val userId = requireNotNull(
            supabaseClient.auth.currentUserOrNull()?.id,
        ) {
            "A signed-in Supabase user is required to sync onboarding"
        }

        val nutritionPayload = buildJsonObject {
            put("user_id", userId)
            draft.textValue(OnboardingStepIds.BodyGoalActivityLevel)
                ?.let { put("activity_level", it) }
            putJsonArray("medical_conditions") {
                draft.selectionValues(OnboardingStepIds.BodyGoalHealthCondition)
                    .filterNot { it == "none" }
                    .forEach { add(JsonPrimitive(it)) }
            }
            draft.decimalValue(OnboardingStepIds.TargetsStepsTarget)
                ?.toInt()
                ?.let { put("steps_target", it) }
            draft.decimalValue(OnboardingStepIds.TargetsWaterTarget)
                ?.toInt()
                ?.let { put("water_target_ml", it) }
        }
        supabaseClient.from("user_nutrition_profiles").upsert(nutritionPayload) {
            onConflict = "user_id"
        }

        if (draft.toggleValue(OnboardingStepIds.WorkoutIntroChoice) == true) {
            val workoutPayload = buildJsonObject {
                put("user_id", userId)
                draft.textValue(OnboardingStepIds.WorkoutExperience)
                    ?.let { put("experience_level", it) }
                draft.textValue(OnboardingStepIds.WorkoutSpecialEventGoal)
                    ?.let { put("special_event_goal", it) }
                draft.textValue(OnboardingStepIds.WorkoutLocation)
                    ?.let { put("workout_location", it) }
                putJsonArray("available_equipment") {
                    draft.selectionValues(OnboardingStepIds.WorkoutEquipment)
                        .forEach { add(JsonPrimitive(it)) }
                }
                draft.decimalValue(OnboardingStepIds.WorkoutDuration)
                    ?.toInt()
                    ?.let { put("workout_duration_mins", it) }
                putJsonArray("training_days") {
                    draft.selectionValues(OnboardingStepIds.WorkoutTrainingDays)
                        .forEach { add(JsonPrimitive(it)) }
                }
                draft.textValue(OnboardingStepIds.WorkoutSplit)
                    ?.let { put("split_program", it) }
                putJsonArray("focus_areas") {
                    draft.selectionValues(OnboardingStepIds.WorkoutFocusAreas)
                        .forEach { add(JsonPrimitive(it)) }
                }
                putJsonArray("health_concerns") {
                    draft.textValue(OnboardingStepIds.WorkoutHealthConcerns)
                        ?.let { add(JsonPrimitive(it)) }
                }
            }
            supabaseClient.from("user_workout_profiles").upsert(workoutPayload) {
                onConflict = "user_id"
            }
        }
    }
}

private fun OnboardingDraft.textValue(stepId: com.tnyx.features.onboarding.domain.model.OnboardingStepId): String? {
    return (answerFor(stepId) as? OnboardingAnswer.Text)?.value?.trim()?.takeIf(String::isNotBlank)
}

private fun OnboardingDraft.decimalValue(stepId: com.tnyx.features.onboarding.domain.model.OnboardingStepId): Double? {
    return (answerFor(stepId) as? OnboardingAnswer.Decimal)?.value
}

private fun OnboardingDraft.selectionValues(stepId: com.tnyx.features.onboarding.domain.model.OnboardingStepId): List<String> {
    return (answerFor(stepId) as? OnboardingAnswer.Selections)?.values.orEmpty()
}

private fun OnboardingDraft.toggleValue(stepId: com.tnyx.features.onboarding.domain.model.OnboardingStepId): Boolean? {
    return (answerFor(stepId) as? OnboardingAnswer.Toggle)?.value
}
