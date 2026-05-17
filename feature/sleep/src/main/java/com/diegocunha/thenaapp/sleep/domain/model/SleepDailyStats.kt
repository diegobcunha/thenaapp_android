package com.diegocunha.thenaapp.sleep.domain.model

data class SleepDailyStats(
    val date: String,
    val totalSleepMinutes: Long,
    val sessionCount: Int,
    val napMinutes: Long,
    val nightSleepMinutes: Long,
    val efficiency: Float,
    val goalMinutes: Int,
    val insight: String?,
)
