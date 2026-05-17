package com.diegocunha.thenaapp.datasource.network.model.sleep

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SleepActiveSessionResponse(
    @SerialName("id")
    val id: String,
    @SerialName("start_time")
    val startTime: String,
    @SerialName("elapsed_minutes")
    val elapsedMinutes: Long,
    @SerialName("sleep_type")
    val sleepType: SleepTypeResponse,
)
