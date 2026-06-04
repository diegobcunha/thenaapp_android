package com.diegocunha.thenaapp.feature.vaccine.presentation

import com.diegocunha.thenaapp.core.mvi.MviIntent
import com.diegocunha.thenaapp.feature.vaccine.domain.model.DoseType
import com.diegocunha.thenaapp.feature.vaccine.domain.model.InjectionSite
import com.diegocunha.thenaapp.feature.vaccine.domain.model.ReactionSeverity

sealed interface RegisterVaccineIntent : MviIntent {
    data class UpdateVaccineName(val name: String) : RegisterVaccineIntent
    data class UpdateDate(val date: String) : RegisterVaccineIntent
    data class UpdateDoseType(val doseType: DoseType) : RegisterVaccineIntent
    data class UpdateInjectionSite(val site: InjectionSite?) : RegisterVaccineIntent
    data class UpdateBatchNumber(val batch: String) : RegisterVaccineIntent
    data class UpdateHealthcareProvider(val provider: String) : RegisterVaccineIntent
    data class ToggleReaction(val hasReaction: Boolean) : RegisterVaccineIntent
    data class UpdateReactionSeverity(val severity: ReactionSeverity) : RegisterVaccineIntent
    data class UpdateReactionNotes(val notes: String) : RegisterVaccineIntent
    object Submit : RegisterVaccineIntent
}