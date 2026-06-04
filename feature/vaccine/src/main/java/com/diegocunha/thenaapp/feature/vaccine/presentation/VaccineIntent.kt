package com.diegocunha.thenaapp.feature.vaccine.presentation

import com.diegocunha.thenaapp.core.mvi.MviIntent

sealed interface VaccineIntent : MviIntent {
    data class SelectTab(val tab: VaccineTab) : VaccineIntent
    object RegisterVaccine : VaccineIntent
    data class RegisterFromSchedule(val pniTemplateId: String) : VaccineIntent
    data class DeleteRecord(val recordId: String) : VaccineIntent
}