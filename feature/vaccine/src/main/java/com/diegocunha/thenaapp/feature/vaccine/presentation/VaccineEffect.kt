package com.diegocunha.thenaapp.feature.vaccine.presentation

import com.diegocunha.thenaapp.core.mvi.MviEffect

sealed interface VaccineEffect : MviEffect {
    data class NavigateToRegister(val babyId: String, val pniTemplateId: String?, val vaccineName: String?) : VaccineEffect
    data class ShowError(val message: Int) : VaccineEffect
}