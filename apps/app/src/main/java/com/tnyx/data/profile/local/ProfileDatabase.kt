package com.tnyx.data.profile.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProfileEntity::class],
    version = ProfileDatabase.VERSION,
    exportSchema = true,
)
abstract class ProfileDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao

    companion object {
        const val VERSION: Int = 1
        const val NAME: String = "tnyx-profile.db"
    }
}
