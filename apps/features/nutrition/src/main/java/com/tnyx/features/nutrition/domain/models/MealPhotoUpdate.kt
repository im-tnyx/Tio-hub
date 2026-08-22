package com.tnyx.features.nutrition.domain.models

sealed interface MealPhotoUpdate {
    data object Unchanged : MealPhotoUpdate
    data object Remove : MealPhotoUpdate

    data class Replace(
        val bytes: ByteArray,
        val mimeType: String,
    ) : MealPhotoUpdate
}
