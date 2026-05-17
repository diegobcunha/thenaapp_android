package com.diegocunha.thenaapp.sleep.domain.model

data class SleepWeeklyStats(
    val days: List<SleepDailyStats>,
    val weeklyAvgMinutes: Long?,
    val trend: String,
)
