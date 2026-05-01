package com.diegocunha.thenaapp.datasource.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breast_segments")
data class BreastSegmentEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val breast: String,
    val startedAt: Long,
    val endedAt: Long? = null,
)