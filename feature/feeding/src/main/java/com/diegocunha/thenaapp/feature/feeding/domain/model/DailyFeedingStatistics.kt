package com.diegocunha.thenaapp.feature.feeding.domain.model

data class DailyFeedingStatistics(
    val date: String,
    val totalSessions: Long,
    val breastfeedingSessions: Long,
    val bottleSessions: Long,
    val totalBreastfeedingDurationSeconds: Long,
    val totalBottleVolumeMl: Long,
)