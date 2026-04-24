package com.diegocunha.thenaapp.datasource.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudinaryResponse(
    @SerialName("secure_url") val secureUrl: String,
    @SerialName("public_id") val publicId: String,
    @SerialName("bytes") val bytes: Int
)
