package com.diegocunha.thenaapp.feature.baby.repository.create

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.safeApiCall
import com.diegocunha.thenaapp.datasource.network.service.BabyService
import com.diegocunha.thenaapp.feature.baby.domain.create.CreateBabyRepository
import com.diegocunha.thenaapp.feature.baby.domain.create.dto.CreateBabyRequest
import com.diegocunha.thenaapp.datasource.network.model.CreateBabyRequest as DatasourceCreateBabyRequest

class CreateBabyRepositoryImpl(
    private val service: BabyService,
    private val dispatchersProvider: DispatchersProvider,
) : CreateBabyRepository {

    override suspend fun createBaby(request: CreateBabyRequest): Resource<Unit> =
        safeApiCall(dispatchersProvider) {
            service.createBaby(
                DatasourceCreateBabyRequest(
                    name = request.name,
                    birthDate = request.birthDate,
                    gender = request.gender.name,
                    responsible = request.responsibleType.name,
//                    photoBase64 = request.photoBase64,
                )
            )
        }
}
