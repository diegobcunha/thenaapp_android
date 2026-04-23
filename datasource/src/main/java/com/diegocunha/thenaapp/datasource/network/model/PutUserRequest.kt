package com.diegocunha.thenaapp.datasource.network.model

import kotlinx.serialization.Serializable


@Serializable
data class PutUserRequest(
    val name: String
)
