package com.tnyx.features.onboarding.domain.usecase

import com.tnyx.features.onboarding.domain.flow.OnboardingStepIds
import com.tnyx.features.onboarding.domain.model.OnboardingAnswer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildReviewSummaryUseCaseTest {
    private val useCase = BuildReviewSummaryUseCase()

    @Test
    fun mapsKnownAnswersIntoDisplaySections() {
        val sections = useCase(
            mapOf(
                OnboardingStepIds.ProfileName to OnboardingAnswer.Text("Santosh"),
                OnboardingStepIds.ProfileGender to OnboardingAnswer.Text("male"),
                OnboardingStepIds.ProfileDateOfBirth to OnboardingAnswer.Text("1990-01-01"),
                OnboardingStepIds.BodyGoalPrimaryGoal to OnboardingAnswer.Text("build_muscle"),
                OnboardingStepIds.BodyGoalHeight to OnboardingAnswer.Decimal(176.0),
                OnboardingStepIds.BodyGoalCurrentWeight to OnboardingAnswer.Decimal(74.5),
                OnboardingStepIds.BodyGoalTargetWeight to OnboardingAnswer.Decimal(70.0),
                OnboardingStepIds.BodyGoalActivityLevel to OnboardingAnswer.Text("active"),
                OnboardingStepIds.BodyGoalHealthCondition to OnboardingAnswer.Selections(
                    listOf("diabetes", "injury_recovery"),
                ),
                OnboardingStepIds.MobileNumber to OnboardingAnswer.Text("+91 9876543210"),
                OnboardingStepIds.WorkoutExperience to OnboardingAnswer.Text("beginner"),
                OnboardingStepIds.WorkoutGymAccess to OnboardingAnswer.Text("both"),
                OnboardingStepIds.WorkoutLocation to OnboardingAnswer.Text("both"),
                OnboardingStepIds.WorkoutFocusAreas to OnboardingAnswer.Selections(
                    listOf("full_body", "shoulders", "arms", "back", "chest", "abs", "glutes", "legs", "cardio"),
                ),
                OnboardingStepIds.WorkoutEquipment to OnboardingAnswer.Selections(
                    listOf("dumbbells", "mat"),
                ),
                OnboardingStepIds.WorkoutTrainingDays to OnboardingAnswer.Selections(
                    listOf("monday", "friday"),
                ),
                OnboardingStepIds.WorkoutDuration to OnboardingAnswer.Decimal(60.0),
                OnboardingStepIds.WorkoutSplit to OnboardingAnswer.Text("upper_lower"),
                OnboardingStepIds.WorkoutHealthConcerns to OnboardingAnswer.Text("Shoulder mobility and old knee discomfort"),
                OnboardingStepIds.WorkoutSpecialEventGoal to OnboardingAnswer.Text("Wedding in December"),
                OnboardingStepIds.TargetsStepsTarget to OnboardingAnswer.Decimal(8500.0),
                OnboardingStepIds.TargetsSleepTarget to OnboardingAnswer.Text("balanced_evenings"),
                OnboardingStepIds.TargetsWaterTarget to OnboardingAnswer.Decimal(2500.0),
                OnboardingStepIds.TargetsGoalPace to OnboardingAnswer.Text("steady"),
                OnboardingStepIds.TargetsNutritionSummary to OnboardingAnswer.Text("protein_priority"),
                OnboardingStepIds.SourceChannel to OnboardingAnswer.Text("friend_referral"),
                OnboardingStepIds.SourceReason to OnboardingAnswer.Text("complete_reset"),
                OnboardingStepIds.SourceReferralDetail to OnboardingAnswer.Text("@fitwithravi"),
            ),
        )

        assertEquals(
            listOf("Profile", "Body goal", "Mobile", "Workout", "Targets", "Source"),
            sections.map { it.title },
        )
        assertEquals("Santosh", sections[0].rows.first { it.label == "Name" }.value)
        assertEquals("Build muscle", sections[1].rows.first { it.label == "Primary goal" }.value)
        assertEquals("176 cm", sections[1].rows.first { it.label == "Height" }.value)
        assertEquals("Diabetes, Injury recovery", sections[1].rows.first { it.label == "Health context" }.value)
        assertEquals("+91 9876543210", sections[2].rows.first { it.label == "Mobile number" }.value)
        assertEquals("Both", sections[3].rows.first { it.label == "Access" }.value)
        assertEquals("Full body, Shoulders, Arms, Back, Chest, Abs, Glutes, Legs, Cardio", sections[3].rows.first { it.label == "Focus areas" }.value)
        assertEquals("Dumbbells, Mat", sections[3].rows.first { it.label == "Equipment" }.value)
        assertEquals("Upper / lower", sections[3].rows.first { it.label == "Split" }.value)
        assertEquals("Shoulder mobility and old knee discomfort", sections[3].rows.first { it.label == "Workout concerns" }.value)
        assertEquals("Wedding in December", sections[3].rows.first { it.label == "Special event" }.value)
        assertEquals("8500 steps", sections[4].rows.first { it.label == "Steps target" }.value)
        assertEquals("Balanced evenings", sections[4].rows.first { it.label == "Sleep target" }.value)
        assertEquals("2500 ml", sections[4].rows.first { it.label == "Water target" }.value)
        assertEquals("Steady", sections[4].rows.first { it.label == "Goal pace" }.value)
        assertEquals("Protein priority", sections[4].rows.first { it.label == "Nutrition focus" }.value)
        assertEquals("Friend referral", sections[5].rows.first { it.label == "Discovery channel" }.value)
        assertEquals("Complete reset", sections[5].rows.first { it.label == "Primary reason" }.value)
        assertEquals("@fitwithravi", sections[5].rows.first { it.label == "Referral detail" }.value)
    }

    @Test
    fun usesFallbackValuesForMissingAnswers() {
        val sections = useCase(emptyMap())

        assertTrue(sections.all { section -> section.rows.isNotEmpty() })
        assertEquals("Not set", sections[0].rows.first { it.label == "Name" }.value)
        assertEquals("Not set", sections[1].rows.first { it.label == "Height" }.value)
        assertEquals("Not set", sections[1].rows.first { it.label == "Health context" }.value)
        assertEquals("Not set", sections[2].rows.first { it.label == "Mobile number" }.value)
        assertEquals("Not set", sections[3].rows.first { it.label == "Access" }.value)
        assertEquals("Not selected", sections[3].rows.first { it.label == "Focus areas" }.value)
        assertEquals("Not selected", sections[3].rows.first { it.label == "Equipment" }.value)
        assertEquals("Not set", sections[3].rows.first { it.label == "Split" }.value)
        assertEquals("Not shared", sections[3].rows.first { it.label == "Workout concerns" }.value)
        assertEquals("Not shared", sections[3].rows.first { it.label == "Special event" }.value)
        assertEquals("Not set", sections[4].rows.first { it.label == "Steps target" }.value)
        assertEquals("Not set", sections[5].rows.first { it.label == "Discovery channel" }.value)
        assertEquals("Not set", sections[5].rows.first { it.label == "Primary reason" }.value)
        assertEquals("Not shared", sections[5].rows.first { it.label == "Referral detail" }.value)
    }

    @Test
    fun hidesEquipmentSummaryWhenGymOnlyAccessWasSelected() {
        val sections = useCase(
            mapOf(
                OnboardingStepIds.WorkoutIntroChoice to OnboardingAnswer.Toggle(true),
                OnboardingStepIds.WorkoutExperience to OnboardingAnswer.Text("beginner"),
                OnboardingStepIds.WorkoutGymAccess to OnboardingAnswer.Text("gym"),
            ),
        )

        assertTrue(sections.first { it.title == "Workout" }.rows.none { it.label == "Equipment" })
    }

    @Test
    fun hidesWorkoutSectionWhenWorkoutPlanWasDeclined() {
        val sections = useCase(
            mapOf(
                OnboardingStepIds.WorkoutIntroChoice to OnboardingAnswer.Toggle(false),
                OnboardingStepIds.TargetsStepsTarget to OnboardingAnswer.Decimal(8500.0),
            ),
        )

        assertTrue(sections.none { it.title == "Workout" })
    }
}
