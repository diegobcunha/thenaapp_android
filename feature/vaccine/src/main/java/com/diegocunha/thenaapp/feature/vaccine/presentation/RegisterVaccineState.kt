package com.diegocunha.thenaapp.feature.vaccine.presentation

import androidx.compose.runtime.Immutable
import com.diegocunha.thenaapp.core.mvi.MviState
import com.diegocunha.thenaapp.feature.vaccine.domain.model.DoseType
import com.diegocunha.thenaapp.feature.vaccine.domain.model.InjectionSite
import com.diegocunha.thenaapp.feature.vaccine.domain.model.ReactionSeverity

@Immutable
data class RegisterVaccineState(
    val vaccineName: String = "",
    val vaccineNameLocked: Boolean = false,
    val administeredDate: String = "",
    val doseType: DoseType = DoseType.PRIMARY,
    val injectionSite: InjectionSite? = null,
    val batchNumber: String = "",
    val healthcareProvider: String = "",
    val hasReaction: Boolean = false,
    val reactionSeverity: ReactionSeverity = ReactionSeverity.MILD,
    val reactionNotes: String = "",
    val isSubmitting: Boolean = false,
    val vaccineNameError: Boolean = false,
    val dateError: Boolean = false,
) : MviState