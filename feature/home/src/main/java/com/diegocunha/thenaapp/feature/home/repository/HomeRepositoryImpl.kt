package com.diegocunha.thenaapp.feature.home.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.database.ActiveFeedingSnapshot
import com.diegocunha.thenaapp.datasource.database.FeedingLocalDataSource
import com.diegocunha.thenaapp.datasource.network.model.baby.BabyResponse
import com.diegocunha.thenaapp.datasource.network.safeApiCall
import com.diegocunha.thenaapp.datasource.network.service.UserService
import com.diegocunha.thenaapp.feature.home.domain.HomeRepository
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeBabyInformation
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeUserInformation
import kotlinx.coroutines.flow.Flow
import java.math.RoundingMode

class HomeRepositoryImpl(
    private val userService: UserService,
    private val dispatchersProvider: DispatchersProvider,
    private val feedingLocalDataSource: FeedingLocalDataSource,
) : HomeRepository {

    override suspend fun getUserInformation(): Resource<HomeUserInformation> =
        safeApiCall(dispatchersProvider) {
            val user = userService.getUsersInformation()
            val babyInfo = user.babies.firstOrNull()
                ?: throw IllegalArgumentException("Baby should not be null")

            HomeUserInformation(
                userName = user.name.orEmpty(),
                babyInformation = babyInfo.toDomain()
            )
        }

    override fun observeActiveFeeding(): Flow<ActiveFeedingSnapshot?> =
        feedingLocalDataSource.observeActiveSession()

    private fun BabyResponse.toDomain() = HomeBabyInformation(
        babyName = name,
        babyBirthDate = birthDate,
        babyPhotoUrl = photoUrl,
        babyHeight = birthHeight.setScale(SCALE_0, RoundingMode.HALF_UP),
        babyWeight = birthWeight.setScale(SCALE_2, RoundingMode.HALF_UP)
    )

    companion object {
        private const val SCALE_0 = 0
        private const val SCALE_2 = 2
    }
}
