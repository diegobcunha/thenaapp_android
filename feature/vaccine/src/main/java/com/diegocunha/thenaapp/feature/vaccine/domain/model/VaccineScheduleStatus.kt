package com.diegocunha.thenaapp.feature.vaccine.domain.model

enum class VaccineScheduleStatus(val value: String) {
    UPCOMING("UPCOMING"),
    DUE("DUE"),
    OVERDUE("OVERDUE"),
    COMPLETED("COMPLETED"),
}