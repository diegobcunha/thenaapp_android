package com.diegocunha.thenaapp.feature.vaccine.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class RegisterVaccineNavigation(
    val babyId: String,
    val pniTemplateId: String? = null,
    val vaccineName: String? = null,
) : NavKey