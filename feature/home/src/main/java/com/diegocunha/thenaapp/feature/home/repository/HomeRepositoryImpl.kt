package com.diegocunha.thenaapp.feature.home.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.database.FeedingLocalDataSource
import com.diegocunha.thenaapp.datasource.database.model.ActiveFeedingSnapshot
import com.diegocunha.thenaapp.datasource.network.model.baby.BabyResponse
import com.diegocunha.thenaapp.datasource.network.safeApiCall
import com.diegocunha.thenaapp.datasource.network.service.SleepApiService
import com.diegocunha.thenaapp.datasource.network.service.UserService
import com.diegocunha.thenaapp.feature.home.domain.HomeRepository
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeBabyInformation
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeUserInformation
import kotlinx.coroutines.flow.Flow
import java.math.RoundingMode
import java.util.Date
import java.util.Locale

class HomeRepositoryImpl(
    private val userService: UserService,
    private val dispatchersProvider: DispatchersProvider,
    private val feedingLocalDataSource: FeedingLocalDataSource,
    private val sleepApiService: SleepApiService,
) : HomeRepository {

    override suspend fun getUserInformation(): Resource<HomeUserInformation> =
        safeApiCall(dispatchersProvider) {
            val user = userService.getUsersInformation()
            val babyInfo = user.babies.firstOrNull()
                ?: throw IllegalArgumentException("Baby should not be null")
            HomeUserInformation(
                userName = user.name.orEmpty(),
                babyInformation = babyInfoToDomain(babyInfo),
            )
        }

    override fun observeActiveFeeding(): Flow<ActiveFeedingSnapshot?> =
        feedingLocalDataSource.observeActiveSession()

    override suspend fun getTodaySleepMinutes(babyId: String): Resource<Int> =
        safeApiCall(dispatchersProvider) {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
            sleepApiService.getDailyStats(babyId, today).totalSleepMinutes.toInt()
        }

    private fun babyInfoToDomain(babyInfo: BabyResponse) = HomeBabyInformation(
        babyId = babyInfo.id.toString(),
        babyName = babyInfo.name,
        babyBirthDate = babyInfo.birthDate,
        babyPhotoUrl = babyInfo.photoUrl,
        babyHeight = babyInfo.birthHeight.setScale(SCALE_0, RoundingMode.HALF_UP),
        babyWeight = babyInfo.birthWeight.setScale(SCALE_2, RoundingMode.HALF_UP),
    )

    companion object {
        private const val SCALE_0 = 0
        private const val SCALE_2 = 2
    }
}
