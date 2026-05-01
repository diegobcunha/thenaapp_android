package com.diegocunha.thenaapp.datasource.network.model.feeding

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BreastSideRequest(
    @SerialName("side")
    val side: BreastSide,
)
