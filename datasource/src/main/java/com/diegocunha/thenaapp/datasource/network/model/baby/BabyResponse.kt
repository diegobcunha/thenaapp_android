package com.diegocunha.thenaapp.datasource.network.model.baby

import com.diegocunha.thenaapp.datasource.network.model.ResponsibleResponse
import com.diegocunha.thenaapp.datasource.serializer.BigDecimalSerializer
import com.diegocunha.thenaapp.datasource.serializer.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.UUID

@Serializable
data class BabyResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    @SerialName("birth_date")
    val birthDate: String,
    @SerialName("sex")
    val gender: String,
    @SerialName("photo_url")
    val photoUrl: String? = null,
    val responsible: List<ResponsibleResponse>,
    @SerialName("birth_weight")
    @Serializable(with = BigDecimalSerializer::class)
    val birthWeight: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("birth_height")
    val birthHeight: BigDecimal

)
