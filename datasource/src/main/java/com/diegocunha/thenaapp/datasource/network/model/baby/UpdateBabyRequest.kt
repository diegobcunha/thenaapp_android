package com.diegocunha.thenaapp.datasource.network.model.baby

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateBabyRequest(
    val name: String? = null,
    @SerialName("photo_url")
    val photoUrl: String? = null,
)
