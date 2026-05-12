package com.diegocunha.thenaapp.datasource.network.model.feeding

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedingStatisticsResponse(
    @SerialName("period_start")
    val periodStart: String,
    @SerialName("period_end")
    val periodEnd: String,
    @SerialName("total_sessions")
    val totalSessions: Long,
    @SerialName("breastfeeding_sessions")
    val breastfeedingSessions: Long,
    @SerialName("bottle_sessions")
    val bottleSessions: Long,
    @SerialName("total_breastfeeding_duration_seconds")
    val totalBreastfeedingDurationSeconds: Long,
    @SerialName("average_breastfeeding_duration_seconds")
    val averageBreastfeedingDurationSeconds: Long,
    @SerialName("total_bottle_volume_ml")
    val totalBottleVolumeMl: Long,
    @SerialName("average_bottle_volume_ml")
    val averageBottleVolumeMl: Long,
)