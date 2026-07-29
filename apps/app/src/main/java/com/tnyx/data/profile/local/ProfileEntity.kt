package com.tnyx.data.profile.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_profiles")
data class ProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "profile_json")
    val profileJson: String,
    @ColumnInfo(name = "updated_at_ms")
    val updatedAtMs: Long,
)
