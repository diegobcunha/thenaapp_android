package com.diegocunha.thenaapp.datasource.network.model.vaccine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterVaccineRequest(
    @SerialName("vaccine_name") val vaccineName: String,
    @SerialName("administered_date") val administeredDate: String,
    @SerialName("dose_type") val doseType: String,
    @SerialName("injection_site") val injectionSite: String? = null,
    @SerialName("batch_number") val batchNumber: String? = null,
    @SerialName("healthcare_provider") val healthcareProvider: String? = null,
    @SerialName("reaction_severity") val reactionSeverity: String? = null,
    @SerialName("reaction_notes") val reactionNotes: String? = null,
    @SerialName("pni_template_id") val pniTemplateId: String? = null,
)