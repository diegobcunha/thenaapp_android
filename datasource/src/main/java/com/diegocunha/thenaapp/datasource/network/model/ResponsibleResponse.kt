package com.diegocunha.thenaapp.datasource.network.model

import com.diegocunha.thenaapp.datasource.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ResponsibleResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val email: String,
    val type: String
)
