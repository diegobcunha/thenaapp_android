package com.diegocunha.thenaapp.datasource.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diegocunha.thenaapp.datasource.database.dao.BreastSegmentDao
import com.diegocunha.thenaapp.datasource.database.dao.FeedingSessionDao
import com.diegocunha.thenaapp.datasource.database.dao.VaccineRecordDao
import com.diegocunha.thenaapp.datasource.database.entity.BreastSegmentEntity
import com.diegocunha.thenaapp.datasource.database.entity.FeedingSessionEntity
import com.diegocunha.thenaapp.datasource.database.entity.VaccineRecordEntity

@Database(
    entities = [
        FeedingSessionEntity::class,
        BreastSegmentEntity::class,
        VaccineRecordEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class ThenaDatabase : RoomDatabase() {
    abstract fun feedingSessionDao(): FeedingSessionDao
    abstract fun breastSegmentDao(): BreastSegmentDao
    abstract fun vaccineRecordDao(): VaccineRecordDao
}