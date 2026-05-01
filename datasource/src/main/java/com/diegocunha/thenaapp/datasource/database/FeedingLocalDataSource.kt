package com.diegocunha.thenaapp.datasource.database

import kotlinx.coroutines.flow.Flow

data class ActiveFeedingSnapshot(
    val sessionId: String,
    val startedAt: Long,
    val activeBreast: String?,
    val type: String,
)

interface FeedingLocalDataSource {
    fun observeActiveSession(): Flow<ActiveFeedingSnapshot?>
}