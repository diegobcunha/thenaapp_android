package com.diegocunha.thenaapp.datasource.network.model.feeding

import com.diegocunha.thenaapp.datasource.serializer.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class UpdateBottleFeedingRequest(
    @SerialName("milk_type")
    val milkType: MilkType? = null,
    @SerialName("volume_ml")
    @Serializable(with = BigDecimalSerializer::class)
    val volumeMl: BigDecimal? = null,
)