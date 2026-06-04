package com.diegocunha.thenaapp.feature.vaccine.presentation

import androidx.compose.runtime.Immutable
import com.diegocunha.thenaapp.core.mvi.MviState
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineRecord
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineScheduleItem
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineScheduleStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class VaccineState(
    val isLoading: Boolean = true,
    val scheduleItems: ImmutableList<VaccineScheduleItem> = persistentListOf(),
    val records: ImmutableList<VaccineRecord> = persistentListOf(),
    val selectedTab: VaccineTab = VaccineTab.UPCOMING,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val hasUrgent: Boolean = false,
    val error: Int? = null,
) : MviState

enum class VaccineTab { UPCOMING, COMPLETED }

val VaccineScheduleItem.isUpcoming get() =
    status == VaccineScheduleStatus.UPCOMING ||
    status == VaccineScheduleStatus.DUE ||
    status == VaccineScheduleStatus.OVERDUE