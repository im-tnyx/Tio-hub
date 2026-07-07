package com.tnyx.di

import com.tnyx.data.profile.SupabaseProfileRepository
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileDataModule {

    @Provides
    @Singleton
    fun provideProfileRepository(supabaseClient: SupabaseClient): ProfileRepository {
        return SupabaseProfileRepository(supabaseClient)
    }
}
