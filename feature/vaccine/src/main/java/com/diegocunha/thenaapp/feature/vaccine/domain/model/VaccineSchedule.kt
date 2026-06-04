package com.diegocunha.thenaapp.feature.vaccine.domain.model

import kotlinx.collections.immutable.ImmutableList

data class VaccineSchedule(
    val items: ImmutableList<VaccineScheduleItem>,
)