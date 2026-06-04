package com.diegocunha.thenaapp.feature.vaccine.domain.model

data class RegisterVaccineInput(
    val babyId: String,
    val vaccineName: String,
    val administeredDate: String,
    val doseType: DoseType,
    val injectionSite: InjectionSite?,
    val batchNumber: String?,
    val healthcareProvider: String?,
    val reactionSeverity: ReactionSeverity?,
    val reactionNotes: String?,
    val pniTemplateId: String?,
)