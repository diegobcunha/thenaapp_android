package com.diegocunha.thenaapp.feature.signup.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class SignupNavigation(
    val isProfileCompletion: Boolean = false,
    val hasBaby: Boolean = false,
) : NavKey
