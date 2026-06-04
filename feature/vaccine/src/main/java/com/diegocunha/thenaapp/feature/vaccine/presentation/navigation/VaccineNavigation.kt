package com.diegocunha.thenaapp.feature.vaccine.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class VaccineNavigation(val babyId: String) : NavKey