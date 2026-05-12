package com.diegocunha.thenaapp.feature.feeding.domain.model

data class ActiveFeedingSession(
    val sessionId: String,
    val type: FeedingType,
    val startedAt: Long,
    val activeBreast: Breast?,
    val leftSegments: List<BreastSegment>,
    val rightSegments: List<BreastSegment>,
)