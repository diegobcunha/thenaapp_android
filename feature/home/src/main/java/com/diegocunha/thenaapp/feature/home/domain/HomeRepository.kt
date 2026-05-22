package com.diegocunha.thenaapp.feature.home.domain

import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.home.domain.dto.ActiveFeedingInfo
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeUserInformation
import kotlinx.coroutines.flow.Flow

interface HomeRepository {

    suspend fun getHomeData(): Resource<HomeUserInformation>
    fun observeActiveFeeding(): Flow<ActiveFeedingInfo?>
    suspend fun closeSleepSession(babyId: String, sessionId: String, endTimeMs: Long): Resource<Unit>
}
