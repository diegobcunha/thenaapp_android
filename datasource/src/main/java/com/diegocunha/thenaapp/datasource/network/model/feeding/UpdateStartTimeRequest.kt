package com.diegocunha.thenaapp.datasource.network.model.feeding

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateStartTimeRequest(
    @SerialName("started_at")
    val startedAt: String,
)