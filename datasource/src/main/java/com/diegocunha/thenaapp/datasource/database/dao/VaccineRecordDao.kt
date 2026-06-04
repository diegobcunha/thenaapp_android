package com.diegocunha.thenaapp.datasource.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.diegocunha.thenaapp.datasource.database.entity.VaccineRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccineRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: VaccineRecordEntity)

    @Query("SELECT * FROM vaccine_records WHERE babyId = :babyId ORDER BY administeredDate DESC")
    fun observeByBaby(babyId: String): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records WHERE id = :id")
    suspend fun getById(id: String): VaccineRecordEntity?

    @Query("DELETE FROM vaccine_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM vaccine_records WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<VaccineRecordEntity>

    @Update
    suspend fun update(record: VaccineRecordEntity)
}