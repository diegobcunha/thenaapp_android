package com.diegocunha.thenaapp.feature.vaccine.presentation

import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.vaccine.R
import com.diegocunha.thenaapp.feature.vaccine.domain.VaccineRepository
import com.diegocunha.thenaapp.feature.vaccine.domain.model.RegisterVaccineInput
import kotlinx.coroutines.launch

class RegisterVaccineViewModel(
    private val repository: VaccineRepository,
    private val babyId: String,
    private val pniTemplateId: String?,
    vaccineName: String?,
) : BaseViewModel<RegisterVaccineState, RegisterVaccineIntent, RegisterVaccineEffect>(
    RegisterVaccineState(
        vaccineName = vaccineName.orEmpty(),
        vaccineNameLocked = vaccineName != null,
    )
) {

    override fun processIntent(intent: RegisterVaccineIntent) {
        when (intent) {
            is RegisterVaccineIntent.UpdateVaccineName ->
                updateState { copy(vaccineName = intent.name, vaccineNameError = false) }
            is RegisterVaccineIntent.UpdateDate ->
                updateState { copy(administeredDate = intent.date, dateError = false) }
            is RegisterVaccineIntent.UpdateDoseType ->
                updateState { copy(doseType = intent.doseType) }
            is RegisterVaccineIntent.UpdateInjectionSite ->
                updateState { copy(injectionSite = intent.site) }
            is RegisterVaccineIntent.UpdateBatchNumber ->
                updateState { copy(batchNumber = intent.batch) }
            is RegisterVaccineIntent.UpdateHealthcareProvider ->
                updateState { copy(healthcareProvider = intent.provider) }
            is RegisterVaccineIntent.ToggleReaction ->
                updateState { copy(hasReaction = intent.hasReaction) }
            is RegisterVaccineIntent.UpdateReactionSeverity ->
                updateState { copy(reactionSeverity = intent.severity) }
            is RegisterVaccineIntent.UpdateReactionNotes ->
                updateState { copy(reactionNotes = intent.notes) }
            RegisterVaccineIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val s = state.value
        val nameInvalid = s.vaccineName.isBlank()
        val dateInvalid = s.administeredDate.length < 8

        if (nameInvalid || dateInvalid) {
            updateState { copy(vaccineNameError = nameInvalid, dateError = dateInvalid) }
            return
        }

        viewModelScope.launch {
            updateState { copy(isSubmitting = true) }
            val input = RegisterVaccineInput(
                babyId = babyId,
                vaccineName = s.vaccineName,
                administeredDate = toIsoDate(s.administeredDate),
                doseType = s.doseType,
                injectionSite = s.injectionSite,
                batchNumber = s.batchNumber.takeIf { it.isNotBlank() },
                healthcareProvider = s.healthcareProvider.takeIf { it.isNotBlank() },
                reactionSeverity = if (s.hasReaction) s.reactionSeverity else null,
                reactionNotes = if (s.hasReaction) s.reactionNotes.takeIf { it.isNotBlank() } else null,
                pniTemplateId = pniTemplateId,
            )
            when (repository.registerVaccine(input)) {
                is Resource.Success -> sendEffect(RegisterVaccineEffect.NavigateBack)
                is Resource.Error -> {
                    updateState { copy(isSubmitting = false) }
                    sendEffect(RegisterVaccineEffect.ShowError(R.string.vaccine_error_register))
                }
                else -> updateState { copy(isSubmitting = false) }
            }
        }
    }

    private fun toIsoDate(digits: String): String {
        if (digits.length < 8) return digits
        val d = digits.filter { it.isDigit() }.take(8)
        return "${d.substring(0, 4)}-${d.substring(4, 6)}-${d.substring(6, 8)}"
    }
}