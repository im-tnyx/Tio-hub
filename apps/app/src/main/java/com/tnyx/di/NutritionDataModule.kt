package com.tnyx.di

import com.tnyx.data.nutrition.NutritionBootstrapRepository
import com.tnyx.data.nutrition.SupabaseFoodSearchRepository
import com.tnyx.data.nutrition.SupabaseMealPhotoRecognitionRepository
import com.tnyx.features.nutrition.domain.repository.FoodSearchRepository
import com.tnyx.features.nutrition.domain.repository.MealPhotoRecognitionRepository
import com.tnyx.features.nutrition.domain.repository.NutritionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NutritionDataModule {

    @Binds
    @Singleton
    abstract fun bindNutritionRepository(
        repository: NutritionBootstrapRepository,
    ): NutritionRepository

    @Binds
    @Singleton
    abstract fun bindFoodSearchRepository(
        repository: SupabaseFoodSearchRepository,
    ): FoodSearchRepository

    @Binds
    @Singleton
    abstract fun bindMealPhotoRecognitionRepository(
        repository: SupabaseMealPhotoRecognitionRepository,
    ): MealPhotoRecognitionRepository

    companion object {
        @Provides
        fun provideCurrentLocalDate(): LocalDate = LocalDate.now()
    }
}
