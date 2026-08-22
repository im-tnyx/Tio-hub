package com.tnyx.features.nutrition.domain.repository

import com.tnyx.features.nutrition.domain.models.MealItem

interface FoodSearchRepository {
    suspend fun search(query: String): List<MealItem>

    suspend fun lookupBarcode(barcode: String): MealItem?
}
