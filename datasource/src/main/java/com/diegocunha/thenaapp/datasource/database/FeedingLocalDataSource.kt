package com.diegocunha.thenaapp.datasource.database

import kotlinx.coroutines.flow.Flow

data class ActiveFeedingSnapshot(
    val sessionId: String,
    val startedAt: Long,
    val activeBreast: String?,
    val type: String,
    val closedSegmentsTotalMs: Long,
    val activeSegmentStartedAt: Long?,
)

interface FeedingLocalDataSource {
    fun observeActiveSession(): Flow<ActiveFeedingSnapshot?>
    suspend fun getActiveSession(): ActiveFeedingSnapshot?
}
