package com.tnyx.di

import com.tnyx.core.ui.shell.domain.repository.BottomNavPreferencesRepository
import com.tnyx.data.preferences.DataStoreBottomNavPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BottomNavPreferencesModule {

    @Binds
    @Singleton
    abstract fun bindBottomNavPreferencesRepository(
        implementation: DataStoreBottomNavPreferencesRepository,
    ): BottomNavPreferencesRepository
}
