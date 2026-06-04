package com.diegocunha.thenaapp.datasource.network.model.vaccine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VaccineScheduleResponse(
    @SerialName("items") val items: List<VaccineScheduleItemResponse>,
)

@Serializable
data class VaccineScheduleItemResponse(
    @SerialName("pni_template_id") val pniTemplateId: String,
    @SerialName("vaccine_name") val vaccineName: String,
    @SerialName("recommended_age_months") val recommendedAgeMonths: Int,
    @SerialName("dose_number") val doseNumber: Int,
    @SerialName("status") val status: String,
    @SerialName("administered_record_id") val administeredRecordId: String? = null,
    @SerialName("administered_date") val administeredDate: String? = null,
)

@Serializable
data class PniTemplateResponse(
    @SerialName("id") val id: String,
    @SerialName("vaccine_name") val vaccineName: String,
    @SerialName("recommended_age_months") val recommendedAgeMonths: Int,
    @SerialName("dose_number") val doseNumber: Int,
    @SerialName("description") val description: String? = null,
)