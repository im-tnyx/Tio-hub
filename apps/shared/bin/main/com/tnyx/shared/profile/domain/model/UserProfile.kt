package com.tnyx.shared.profile.domain.model

data class UserProfile(
    val id: String,
    val displayName: String,
    val dob: String,
    val gender: String,
    val planLabel: String,
    val weight: Double,
    val height: Int,
    val bmi: Double,
    val bmr: Int
)
