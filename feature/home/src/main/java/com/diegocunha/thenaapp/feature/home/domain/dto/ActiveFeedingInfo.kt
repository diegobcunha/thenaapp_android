package com.diegocunha.thenaapp.feature.home.domain.dto

data class ActiveFeedingInfo(
    val activeBreast: String?,
    val closedSegmentsTotalMs: Long,
    val activeSegmentStartedAt: Long?,
) {
    fun elapsedSeconds(): Long {
        val segStart = activeSegmentStartedAt
        return if (segStart != null) {
            (closedSegmentsTotalMs + System.currentTimeMillis() - segStart) / 1_000L
        } else {
            closedSegmentsTotalMs / 1_000L
        }
    }
}
