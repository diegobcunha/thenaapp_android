package com.diegocunha.thenaapp.datasource.network.model.sleep

import kotlinx.serialization.Serializable

@Serializable
enum class SleepTypeResponse {
    NAP, NIGHT_SLEEP, EARLY_MORNING, CATNAP, CONTACT_NAP, CAR_NAP
}
