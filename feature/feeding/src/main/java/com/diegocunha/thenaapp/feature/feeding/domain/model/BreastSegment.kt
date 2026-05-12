package com.diegocunha.thenaapp.feature.feeding.domain.model

data class BreastSegment(
    val id: String,
    val breast: Breast,
    val startedAt: Long,
    val endedAt: Long?,
)