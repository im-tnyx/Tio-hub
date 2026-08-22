package com.tnyx.features.nutrition.domain.models

import androidx.compose.runtime.Immutable

@Immutable
data class MealPhotoAnalysis(
    val suggestedName: String,
    val items: List<MealItem>,
)
