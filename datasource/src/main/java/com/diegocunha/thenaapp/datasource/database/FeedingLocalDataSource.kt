package com.diegocunha.thenaapp.datasource.database

import com.diegocunha.thenaapp.datasource.database.model.ActiveFeedingSnapshot
import kotlinx.coroutines.flow.Flow

interface FeedingLocalDataSource {
    fun observeActiveSession(): Flow<ActiveFeedingSnapshot?>
    suspend fun getActiveSession(): ActiveFeedingSnapshot?
}
