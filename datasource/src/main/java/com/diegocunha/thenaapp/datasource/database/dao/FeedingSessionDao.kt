package com.diegocunha.thenaapp.datasource.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diegocunha.thenaapp.datasource.database.entity.FeedingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedingSessionDao {
    @Insert
    suspend fun insert(session: FeedingSessionEntity)

    @Update
    suspend fun update(session: FeedingSessionEntity)

    @Query("SELECT * FROM feeding_sessions WHERE endedAt IS NULL LIMIT 1")
    fun observeActiveSession(): Flow<FeedingSessionEntity?>

    @Query("SELECT * FROM feeding_sessions WHERE id = :id")
    suspend fun getById(id: String): FeedingSessionEntity?

    @Query("SELECT * FROM feeding_sessions ORDER BY startedAt DESC")
    fun observeAllSessions(): Flow<List<FeedingSessionEntity>>
}