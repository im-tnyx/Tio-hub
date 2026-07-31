package com.tnyx.features.onboarding.domain.model

data class OnboardingTargetSnapshot(
    val stepsTarget: Int,
    val sleepTargetId: String,
    val waterTargetMl: Int,
    val caloriesTarget: Int,
    val proteinTargetGrams: Int,
    val carbsTargetGrams: Int,
    val fatTargetGrams: Int,
    val fiberTargetGrams: Int,
    val goalPaceKgPerWeek: Double,
)
