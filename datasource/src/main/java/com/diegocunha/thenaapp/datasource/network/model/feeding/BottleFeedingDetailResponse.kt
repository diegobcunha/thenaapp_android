package com.diegocunha.thenaapp.datasource.network.model.feeding

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BottleFeedingDetailResponse(
    @SerialName("milk_type")
    val milkType: MilkType,
    @SerialName("volume_ml")
    val volumeMl: Double,
)
