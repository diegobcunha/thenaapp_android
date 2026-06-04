package com.diegocunha.thenaapp.feature.vaccine.presentation

import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.vaccine.R
import com.diegocunha.thenaapp.feature.vaccine.domain.VaccineRepository
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineScheduleStatus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VaccineViewModel(
    private val repository: VaccineRepository,
    private val babyId: String,
) : BaseViewModel<VaccineState, VaccineIntent, VaccineEffect>(VaccineState()) {

    init {
        observeRecords()
        loadSchedule()
        syncPending()
    }

    override fun processIntent(intent: VaccineIntent) {
        when (intent) {
            is VaccineIntent.SelectTab -> updateState { copy(selectedTab = intent.tab) }
            VaccineIntent.RegisterVaccine ->
                sendEffect(VaccineEffect.NavigateToRegister(babyId, null, null))
            is VaccineIntent.RegisterFromSchedule -> {
                val name = state.value.scheduleItems
                    .firstOrNull { it.pniTemplateId == intent.pniTemplateId }?.vaccineName
                sendEffect(VaccineEffect.NavigateToRegister(babyId, intent.pniTemplateId, name))
            }
            is VaccineIntent.DeleteRecord -> deleteRecord(intent.recordId)
        }
    }

    private fun observeRecords() {
        viewModelScope.launch {
            repository.observeRecords(babyId).collectLatest { records ->
                val completed = records.size
                updateState {
                    copy(
                        records = records.toPersistentList(),
                        completedCount = completed,
                    )
                }
            }
        }
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            when (val result = repository.getSchedule(babyId)) {
                is Resource.Success -> {
                    val items = result.data.items
                    val total = items.size
                    val hasUrgent = items.any {
                        it.status == VaccineScheduleStatus.OVERDUE || it.status == VaccineScheduleStatus.DUE
                    }
                    updateState {
                        copy(
                            isLoading = false,
                            scheduleItems = items,
                            totalCount = total,
                            hasUrgent = hasUrgent,
                        )
                    }
                }
                is Resource.Error -> updateState {
                    copy(isLoading = false, error = R.string.vaccine_error_load)
                }
                else -> Unit
            }
        }
    }

    private fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            when (repository.deleteRecord(babyId, recordId)) {
                is Resource.Error -> sendEffect(VaccineEffect.ShowError(R.string.vaccine_error_delete))
                else -> Unit
            }
        }
    }

    private fun syncPending() {
        viewModelScope.launch { repository.syncPendingRecords(babyId) }
    }
}