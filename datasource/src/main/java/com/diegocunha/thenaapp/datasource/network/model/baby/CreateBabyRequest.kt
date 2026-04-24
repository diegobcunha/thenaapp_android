package com.diegocunha.thenaapp.datasource.network.model.baby

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
)
