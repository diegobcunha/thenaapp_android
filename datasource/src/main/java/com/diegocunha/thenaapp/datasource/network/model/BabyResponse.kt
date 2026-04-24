package com.diegocunha.thenaapp.datasource.network.model

import com.diegocunha.thenaapp.datasource.serializer.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class BabyResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    @SerialName("birth_date")
    val birthDate: String,
    val gender: String,
    val photoUrl: String,
    val responsible: List<ResponsibleResponse>
)
