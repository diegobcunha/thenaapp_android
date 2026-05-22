package com.diegocunha.thenaapp.datasource.network.model.home

import com.diegocunha.thenaapp.datasource.serializer.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ActiveSleepSessionDto(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @SerialName("start_time")
    val startTime: String,
    @SerialName("sleep_type")
    val sleepType: String,
)