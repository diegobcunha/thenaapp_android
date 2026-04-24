package com.diegocunha.thenaapp.datasource.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBabyRequest(
    val name: String,
    @SerialName("birth_date")
    val birthDate: String,
    val gender: String,
    @SerialName("responsible_type")
    val responsible: String,
    @SerialName("photo_url")
    val photoBase64: String? = null,
)
