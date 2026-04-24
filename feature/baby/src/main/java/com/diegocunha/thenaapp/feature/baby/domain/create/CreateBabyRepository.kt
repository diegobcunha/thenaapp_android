package com.diegocunha.thenaapp.feature.baby.domain.create

import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.baby.domain.create.dto.CreateBabyRequest

interface CreateBabyRepository {

    suspend fun createBaby(request: CreateBabyRequest): Resource<Unit>
}
