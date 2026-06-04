package com.diegocunha.thenaapp.feature.vaccine.domain.model

data class VaccineScheduleItem(
    val pniTemplateId: String,
    val vaccineName: String,
    val recommendedAgeMonths: Int,
    val doseNumber: Int,
    val status: VaccineScheduleStatus,
    val administeredRecordId: String?,
    val administeredDate: String?,
)