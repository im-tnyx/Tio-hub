package com.tnyx.features.nutrition.domain.repository

import com.tnyx.features.nutrition.domain.models.MealPhotoAnalysis

interface MealPhotoRecognitionRepository {
    suspend fun analyze(
        imageBytes: ByteArray,
        mimeType: String,
    ): MealPhotoAnalysis
}

class MealPhotoAnalysisException(message: String) : RuntimeException(message)
