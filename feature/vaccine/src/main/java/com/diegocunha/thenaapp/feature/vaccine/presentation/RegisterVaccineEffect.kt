package com.diegocunha.thenaapp.feature.vaccine.presentation

import com.diegocunha.thenaapp.core.mvi.MviEffect

sealed interface RegisterVaccineEffect : MviEffect {
    object NavigateBack : RegisterVaccineEffect
    data class ShowError(val message: Int) : RegisterVaccineEffect
}