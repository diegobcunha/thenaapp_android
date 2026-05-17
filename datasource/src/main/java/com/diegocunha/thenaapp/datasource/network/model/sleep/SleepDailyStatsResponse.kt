package com.diegocunha.thenaapp.datasource.network.model.sleep

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SleepDailyStatsResponse(
    @SerialName("date") val date: String,
    @SerialName("total_sleep_minutes") val totalSleepMinutes: Long = 0L,
    @SerialName("total_nap_count") val sessionCount: Int = 0,
    @SerialName("total_nap_minutes") val napMinutes: Long? = null,
    @SerialName("night_sleep_minutes") val nightSleepMinutes: Long? = null,
    @SerialName("sleep_efficiency_percent") val efficiencyPercent: Float? = null,
    @SerialName("insight") val insight: String? = null,
)
