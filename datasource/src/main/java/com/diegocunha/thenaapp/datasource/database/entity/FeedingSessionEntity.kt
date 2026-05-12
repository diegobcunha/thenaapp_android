package com.diegocunha.thenaapp.datasource.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeding_sessions")
data class FeedingSessionEntity(
    @PrimaryKey val id: String,
    val babyId: String,
    val type: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val bottleMl: Int? = null,
    val bottleType: String? = null,
    val synced: Boolean = false,
)