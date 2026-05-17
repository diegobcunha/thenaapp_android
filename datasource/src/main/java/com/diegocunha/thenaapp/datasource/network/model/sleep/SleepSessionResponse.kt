package com.diegocunha.thenaapp.datasource.network.model.sleep

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SleepSessionResponse(
    val id: String,
    @SerialName("start_time")
    val startTime: String,
    @SerialName("end_time")
    val endTime: String? = null,
    @SerialName("duration_minutes")
    val durationMinutes: Long? = null,
    @SerialName("sleep_type")
    val sleepType: SleepTypeResponse,
    @SerialName("is_active")
    val isActive: Boolean,
)
