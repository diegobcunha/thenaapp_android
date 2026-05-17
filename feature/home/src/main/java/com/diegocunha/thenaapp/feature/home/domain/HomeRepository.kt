package com.diegocunha.thenaapp.feature.home.domain

import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.database.model.ActiveFeedingSnapshot
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeUserInformation
import kotlinx.coroutines.flow.Flow

interface HomeRepository {

    suspend fun getUserInformation(): Resource<HomeUserInformation>
    fun observeActiveFeeding(): Flow<ActiveFeedingSnapshot?>
    suspend fun getTodaySleepMinutes(babyId: String): Resource<Int>
}
