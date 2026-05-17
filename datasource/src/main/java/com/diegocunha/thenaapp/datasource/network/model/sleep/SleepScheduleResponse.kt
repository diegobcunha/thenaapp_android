package com.diegocunha.thenaapp.datasource.network.model.sleep

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SleepScheduleResponse(
    @SerialName("date") val date: String,
    @SerialName("naps") val naps: List<NapScheduleItemResponse>,
    @SerialName("night_sleep_minutes") val nightSleepMinutes: Int,
    @SerialName("total_sleep_minutes") val totalSleepMinutes: Int,
)
