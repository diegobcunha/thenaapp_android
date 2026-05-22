package com.diegocunha.thenaapp.datasource.network.model.home

import com.diegocunha.thenaapp.datasource.serializer.BigDecimalSerializer
import com.diegocunha.thenaapp.datasource.serializer.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.UUID

@Serializable
data class BabyHomeDto(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    @SerialName("birth_date")
    val birthDate: String,
    val sex: String,
    @SerialName("photo_url")
    val photoUrl: String? = null,
    @SerialName("birth_weight")
    @Serializable(with = BigDecimalSerializer::class)
    val birthWeight: BigDecimal,
    @SerialName("birth_height")
    @Serializable(with = BigDecimalSerializer::class)
    val birthHeight: BigDecimal,
)
