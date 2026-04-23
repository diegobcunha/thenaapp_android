package com.diegocunha.thenaapp.feature.signup.domain

import kotlinx.serialization.Serializable

@Serializable
data class GoogleSignUpResponse(
    val email: String
)
