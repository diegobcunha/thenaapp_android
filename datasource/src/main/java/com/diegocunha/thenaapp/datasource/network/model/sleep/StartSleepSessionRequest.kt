package com.diegocunha.thenaapp.datasource.network.model.sleep

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartSleepSessionRequest(
    @SerialName("start_time")
    val startTime: String,
    @SerialName("end_time")
    val endTime: String? = null,
    @SerialName("timezone")
    val timezone: String,
    @SerialName("sleep_type")
    val sleepType: SleepTypeResponse = SleepTypeResponse.NAP,
)

