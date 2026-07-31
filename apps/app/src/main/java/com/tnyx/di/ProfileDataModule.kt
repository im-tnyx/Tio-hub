package com.tnyx.di

import android.content.Context
import androidx.room.Room
import com.tnyx.data.profile.InternalProfileAvatarStore
import com.tnyx.data.profile.ProfilePersistenceCodec
import com.tnyx.data.profile.RoomProfileRepository
import com.tnyx.data.profile.SupabaseProfileRepository
import com.tnyx.data.profile.local.ProfileDao
import com.tnyx.data.profile.local.ProfileDatabase
import io.github.jan.supabase.SupabaseClient
import com.tnyx.shared.auth.domain.repository.AuthSessionProvider
import com.tnyx.shared.profile.domain.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileDataModule {

    @Provides
    @Singleton
    fun provideProfileDatabase(
        @ApplicationContext context: Context,
    ): ProfileDatabase {
        return Room.databaseBuilder(
            context,
            ProfileDatabase::class.java,
            ProfileDatabase.NAME,
        ).build()
    }

    @Provides
    fun provideProfileDao(database: ProfileDatabase): ProfileDao = database.profileDao()

    @Provides
    @Singleton
    fun provideProfilePersistenceCodec(): ProfilePersistenceCodec = ProfilePersistenceCodec()

    @Provides
    @Singleton
    fun provideInternalProfileAvatarStore(
        @ApplicationContext context: Context,
    ): InternalProfileAvatarStore = InternalProfileAvatarStore(context)

    @Provides
    @Singleton
    fun provideProfileRepository(
        supabaseClient: SupabaseClient,
        sessionProvider: AuthSessionProvider,
    ): ProfileRepository {
        return SupabaseProfileRepository(
            supabaseClient = supabaseClient,
            sessionProvider = sessionProvider,
        )
    }
}
