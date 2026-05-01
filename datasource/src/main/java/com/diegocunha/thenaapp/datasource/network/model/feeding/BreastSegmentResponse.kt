package com.diegocunha.thenaapp.datasource.network.model.feeding

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BreastSegmentResponse(
    @SerialName("id")
    val id: String,
    @SerialName("side")
    val side: BreastSide,
    @SerialName("started_at")
    val startedAt: String,
    @SerialName("ended_at")
    val endedAt: String? = null,
    @SerialName("duration_seconds")
    val durationSeconds: Long? = null,
)
