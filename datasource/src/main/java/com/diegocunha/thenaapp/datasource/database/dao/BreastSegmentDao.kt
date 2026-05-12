package com.diegocunha.thenaapp.datasource.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diegocunha.thenaapp.datasource.database.entity.BreastSegmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BreastSegmentDao {
    @Insert
    suspend fun insert(segment: BreastSegmentEntity)

    @Update
    suspend fun update(segment: BreastSegmentEntity)

    @Query("SELECT * FROM breast_segments WHERE sessionId = :sessionId")
    fun observeBySession(sessionId: String): Flow<List<BreastSegmentEntity>>

    @Query("SELECT * FROM breast_segments WHERE sessionId = :sessionId")
    suspend fun getBySession(sessionId: String): List<BreastSegmentEntity>

    @Query("SELECT * FROM breast_segments WHERE id = :id")
    suspend fun getById(id: String): BreastSegmentEntity?

    @Query("SELECT * FROM breast_segments WHERE sessionId = :sessionId AND endedAt IS NULL LIMIT 1")
    suspend fun getActiveSegment(sessionId: String): BreastSegmentEntity?
}
