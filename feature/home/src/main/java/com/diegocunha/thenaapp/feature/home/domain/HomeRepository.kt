package com.diegocunha.thenaapp.feature.home.domain

import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeUserInformation

interface HomeRepository {

    suspend fun getUserInformation(): Resource<HomeUserInformation>
}
