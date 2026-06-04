package com.diegocunha.thenaapp.feature.vaccine.domain.model

data class PniTemplate(
    val id: String,
    val vaccineName: String,
    val recommendedAgeMonths: Int,
    val doseNumber: Int,
    val description: String?,
)