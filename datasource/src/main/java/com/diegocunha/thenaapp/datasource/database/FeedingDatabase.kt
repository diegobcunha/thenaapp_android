package com.diegocunha.thenaapp.datasource.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diegocunha.thenaapp.datasource.database.dao.BreastSegmentDao
import com.diegocunha.thenaapp.datasource.database.dao.FeedingSessionDao
import com.diegocunha.thenaapp.datasource.database.entity.BreastSegmentEntity
import com.diegocunha.thenaapp.datasource.database.entity.FeedingSessionEntity

@Database(
    entities = [FeedingSessionEntity::class, BreastSegmentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FeedingDatabase : RoomDatabase() {
    abstract fun feedingSessionDao(): FeedingSessionDao
    abstract fun breastSegmentDao(): BreastSegmentDao
}