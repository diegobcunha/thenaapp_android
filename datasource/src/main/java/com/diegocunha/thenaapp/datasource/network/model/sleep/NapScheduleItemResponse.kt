package com.diegocunha.thenaapp.datasource.network.model.sleep

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NapScheduleItemResponse(
    @SerialName("start_time") val startTime: String,
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("sleep_type") val sleepType: SleepTypeResponse,
)
