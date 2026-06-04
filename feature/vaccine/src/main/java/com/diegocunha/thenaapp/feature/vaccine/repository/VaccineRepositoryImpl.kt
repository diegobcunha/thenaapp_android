package com.diegocunha.thenaapp.feature.vaccine.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.database.dao.VaccineRecordDao
import com.diegocunha.thenaapp.datasource.database.entity.VaccineRecordEntity
import com.diegocunha.thenaapp.datasource.network.model.vaccine.RegisterVaccineRequest
import com.diegocunha.thenaapp.datasource.network.model.vaccine.VaccineRecordResponse
import com.diegocunha.thenaapp.datasource.network.model.vaccine.VaccineScheduleItemResponse
import com.diegocunha.thenaapp.datasource.network.safeApiCall
import com.diegocunha.thenaapp.datasource.network.service.VaccineApiService
import com.diegocunha.thenaapp.feature.vaccine.domain.VaccineRepository
import com.diegocunha.thenaapp.feature.vaccine.domain.model.DoseType
import com.diegocunha.thenaapp.feature.vaccine.domain.model.InjectionSite
import com.diegocunha.thenaapp.feature.vaccine.domain.model.ReactionSeverity
import com.diegocunha.thenaapp.feature.vaccine.domain.model.RegisterVaccineInput
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineRecord
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineSchedule
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineScheduleItem
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineScheduleStatus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class VaccineRepositoryImpl(
    private val vaccineApiService: VaccineApiService,
    private val vaccineRecordDao: VaccineRecordDao,
    private val dispatchersProvider: DispatchersProvider,
) : VaccineRepository {

    override fun observeRecords(babyId: String): Flow<List<VaccineRecord>> =
        vaccineRecordDao.observeByBaby(babyId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getSchedule(babyId: String): Resource<VaccineSchedule> =
        safeApiCall(dispatchersProvider) {
            val response = vaccineApiService.getSchedule(babyId)
            VaccineSchedule(items = response.items.map { it.toDomain() }.toPersistentList())
        }

    override suspend fun registerVaccine(input: RegisterVaccineInput): Resource<VaccineRecord> {
        val localId = UUID.randomUUID().toString()
        val entity = input.toEntity(localId)
        withContext(dispatchersProvider.io()) {
            vaccineRecordDao.upsert(entity)
        }

        return when (val result = safeApiCall(dispatchersProvider) {
            vaccineApiService.registerVaccine(input.babyId, input.toRequest()).toDomain(input.babyId)
        }) {
            is Resource.Success -> {
                val synced = entity.copy(serverId = result.data.id, pendingSync = false)
                withContext(dispatchersProvider.io()) { vaccineRecordDao.upsert(synced) }
                Resource.Success(result.data.copy(id = localId))
            }
            is Resource.Error -> Resource.Error(result.exception)
            else -> result
        }
    }

    override suspend fun deleteRecord(babyId: String, recordId: String): Resource<Unit> {
        withContext(dispatchersProvider.io()) { vaccineRecordDao.deleteById(recordId) }
        val entity = withContext(dispatchersProvider.io()) { vaccineRecordDao.getById(recordId) }
        val serverId = entity?.serverId ?: return Resource.Success(Unit)
        return safeApiCall(dispatchersProvider) {
            vaccineApiService.deleteRecord(babyId, serverId)
        }
    }

    override suspend fun syncPendingRecords(babyId: String) {
        val pending = withContext(dispatchersProvider.io()) { vaccineRecordDao.getPendingSync() }
        pending.forEach { entity ->
            val result = safeApiCall(dispatchersProvider) {
                vaccineApiService.registerVaccine(babyId, entity.toRequest()).toDomain(babyId)
            }
            if (result is Resource.Success) {
                vaccineRecordDao.upsert(entity.copy(serverId = result.data.id, pendingSync = false))
            }
        }
    }

    private fun VaccineRecordEntity.toDomain() = VaccineRecord(
        id = id,
        babyId = babyId,
        vaccineName = vaccineName,
        administeredDate = administeredDate,
        doseType = DoseType.entries.firstOrNull { it.value == doseType } ?: DoseType.PRIMARY,
        injectionSite = injectionSite?.let { site -> InjectionSite.entries.firstOrNull { it.value == site } },
        batchNumber = batchNumber,
        healthcareProvider = healthcareProvider,
        reactionSeverity = reactionSeverity?.let { sev -> ReactionSeverity.entries.firstOrNull { it.value == sev } },
        reactionNotes = reactionNotes,
        pniTemplateId = pniTemplateId,
        pendingSync = pendingSync,
    )

    private fun VaccineRecordResponse.toDomain(babyId: String) = VaccineRecord(
        id = id,
        babyId = babyId,
        vaccineName = vaccineName,
        administeredDate = administeredDate,
        doseType = DoseType.entries.firstOrNull { it.value == doseType } ?: DoseType.PRIMARY,
        injectionSite = injectionSite?.let { site -> InjectionSite.entries.firstOrNull { it.value == site } },
        batchNumber = batchNumber,
        healthcareProvider = healthcareProvider,
        reactionSeverity = reactionSeverity?.let { sev -> ReactionSeverity.entries.firstOrNull { it.value == sev } },
        reactionNotes = reactionNotes,
        pniTemplateId = pniTemplateId,
        pendingSync = false,
    )

    private fun VaccineScheduleItemResponse.toDomain() = VaccineScheduleItem(
        pniTemplateId = pniTemplateId,
        vaccineName = vaccineName,
        recommendedAgeMonths = recommendedAgeMonths,
        doseNumber = doseNumber,
        status = VaccineScheduleStatus.entries.firstOrNull { it.value == status } ?: VaccineScheduleStatus.UPCOMING,
        administeredRecordId = administeredRecordId,
        administeredDate = administeredDate,
    )

    private fun RegisterVaccineInput.toEntity(localId: String) = VaccineRecordEntity(
        id = localId,
        babyId = babyId,
        vaccineName = vaccineName,
        administeredDate = administeredDate,
        doseType = doseType.value,
        injectionSite = injectionSite?.value,
        batchNumber = batchNumber,
        healthcareProvider = healthcareProvider,
        reactionSeverity = reactionSeverity?.value,
        reactionNotes = reactionNotes,
        pniTemplateId = pniTemplateId,
        serverId = null,
        pendingSync = true,
    )

    private fun RegisterVaccineInput.toRequest() = RegisterVaccineRequest(
        vaccineName = vaccineName,
        administeredDate = administeredDate,
        doseType = doseType.value,
        injectionSite = injectionSite?.value,
        batchNumber = batchNumber,
        healthcareProvider = healthcareProvider,
        reactionSeverity = reactionSeverity?.value,
        reactionNotes = reactionNotes,
        pniTemplateId = pniTemplateId,
    )

    private fun VaccineRecordEntity.toRequest() = RegisterVaccineRequest(
        vaccineName = vaccineName,
        administeredDate = administeredDate,
        doseType = doseType,
        injectionSite = injectionSite,
        batchNumber = batchNumber,
        healthcareProvider = healthcareProvider,
        reactionSeverity = reactionSeverity,
        reactionNotes = reactionNotes,
        pniTemplateId = pniTemplateId,
    )
}