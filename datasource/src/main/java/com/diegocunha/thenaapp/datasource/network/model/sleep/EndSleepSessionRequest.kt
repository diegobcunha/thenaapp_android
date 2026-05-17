package com.diegocunha.thenaapp.datasource.network.model.sleep

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EndSleepSessionRequest(
    @SerialName("end_time")
    val endTime: String,
)
