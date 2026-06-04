package com.diegocunha.thenaapp.feature.vaccine.domain

import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.vaccine.domain.model.RegisterVaccineInput
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineRecord
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineSchedule
import kotlinx.coroutines.flow.Flow

interface VaccineRepository {
    fun observeRecords(babyId: String): Flow<List<VaccineRecord>>
    suspend fun getSchedule(babyId: String): Resource<VaccineSchedule>
    suspend fun registerVaccine(input: RegisterVaccineInput): Resource<VaccineRecord>
    suspend fun deleteRecord(babyId: String, recordId: String): Resource<Unit>
    suspend fun syncPendingRecords(babyId: String)
}