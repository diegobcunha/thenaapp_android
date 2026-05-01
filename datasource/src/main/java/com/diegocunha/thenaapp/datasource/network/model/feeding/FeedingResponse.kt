package com.diegocunha.thenaapp.datasource.network.model.feeding

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedingSessionResponse(
    @SerialName("id")
    val id: String,
    @SerialName("type")
    val type: FeedingType,
    @SerialName("status")
    val status: FeedingStatus,
    @SerialName("started_at")
    val startedAt: String,
    @SerialName("ended_at")
    val endedAt: String? = null,
    @SerialName("total_duration_seconds")
    val totalDurationSeconds: Long,
    @SerialName("breast_segments")
    val breastSegments: List<BreastSegmentResponse>,
    @SerialName("bottle_detail")
    val bottleDetail: BottleFeedingDetailResponse? = null,
)

