package com.diegocunha.thenaapp.datasource.network.model.baby

import com.diegocunha.thenaapp.datasource.serializer.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class CreateBabyRequest(
    val name: String,
    @SerialName("birth_date")
    val birthDate: String,
    @SerialName("sex")
    val gender: String,
    @SerialName("responsible_type")
    val responsible: String,
    @SerialName("photo_url")
    val photoBase64: String? = null,
    @SerialName("birth_weight")
    @Serializable(with = BigDecimalSerializer::class)
    val birthWeight: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("birth_height")
    val birthHeight: BigDecimal
)
