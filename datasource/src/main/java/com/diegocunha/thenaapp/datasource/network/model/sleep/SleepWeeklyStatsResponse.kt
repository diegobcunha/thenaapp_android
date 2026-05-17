package com.diegocunha.thenaapp.datasource.network.model.sleep

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SleepWeeklyStatsResponse(
    @SerialName("days") val days: List<SleepDailyStatsResponse>,
    @SerialName("weekly_avg_sleep_minutes") val weeklyAvgMinutes: Long = 0L,
    @SerialName("trend") val trend: String,
)
