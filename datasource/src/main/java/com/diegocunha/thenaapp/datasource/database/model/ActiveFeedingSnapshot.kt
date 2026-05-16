package com.diegocunha.thenaapp.datasource.database.model

data class ActiveFeedingSnapshot(
    val sessionId: String,
    val startedAt: Long,
    val activeBreast: String?,
    val type: String,
    val closedSegmentsTotalMs: Long,
    val activeSegmentStartedAt: Long?,
)
