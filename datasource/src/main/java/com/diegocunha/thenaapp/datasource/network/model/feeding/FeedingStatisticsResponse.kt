package com.diegocunha.thenaapp.datasource.network.model.feeding

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedingStatisticsResponse(
    @SerialName("periodStart")
    val periodStart: String,
    @SerialName("periodEnd")
    val periodEnd: String,
    @SerialName("totalSessions")
    val totalSessions: Long,
    @SerialName("breastfeedingSessions")
    val breastfeedingSessions: Long,
    @SerialName("bottleSessions")
    val bottleSessions: Long,
    @SerialName("totalBreastfeedingDurationSeconds")
    val totalBreastfeedingDurationSeconds: Long,
    @SerialName("averageBreastfeedingDurationSeconds")
    val averageBreastfeedingDurationSeconds: Long,
    @SerialName("totalBottleVolumeMl")
    val totalBottleVolumeMl: Long,
    @SerialName("averageBottleVolumeMl")
    val averageBottleVolumeMl: Long,
)