package com.tnyx.data.profile.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM local_profiles WHERE user_id = :userId LIMIT 1")
    fun observeProfile(userId: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM local_profiles WHERE user_id = :userId LIMIT 1")
    suspend fun getProfile(userId: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)
}
