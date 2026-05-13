package com.diegocunha.thenaapp.feature.feeding.domain.model

data class FeedingStatistics(
    val periodStart: String,
    val periodEnd: String,
    val totalSessions: Long,
    val breastfeedingSessions: Long,
    val bottleSessions: Long,
    val totalBreastfeedingDurationSeconds: Long,
    val averageBreastfeedingDurationSeconds: Long,
    val totalBottleVolumeMl: Long,
    val averageBottleVolumeMl: Long,
    val volumeByMilkType: Map<String, Long>,
    val dailyBreakdown: List<DailyFeedingStatistics>,
)
