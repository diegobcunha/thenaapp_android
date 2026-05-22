package com.diegocunha.thenaapp.feature.home.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.database.FeedingLocalDataSource
import com.diegocunha.thenaapp.datasource.network.model.home.ActiveSleepSessionDto
import com.diegocunha.thenaapp.datasource.network.model.home.BabyHomeDto
import com.diegocunha.thenaapp.datasource.network.model.sleep.EndSleepSessionRequest
import com.diegocunha.thenaapp.datasource.network.safeApiCall
import com.diegocunha.thenaapp.datasource.network.service.HomeService
import com.diegocunha.thenaapp.datasource.network.service.SleepApiService
import com.diegocunha.thenaapp.feature.home.domain.HomeRepository
import com.diegocunha.thenaapp.feature.home.domain.dto.ActiveFeedingInfo
import com.diegocunha.thenaapp.feature.home.domain.dto.ActiveSleepSessionInfo
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeBabyInformation
import com.diegocunha.thenaapp.feature.home.domain.dto.HomeUserInformation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone



class HomeRepositoryImpl(
    private val homeService: HomeService,
    private val sleepApiService: SleepApiService,
    private val dispatchersProvider: DispatchersProvider,
    private val feedingLocalDataSource: FeedingLocalDataSource,
) : HomeRepository {

    override suspend fun getHomeData(): Resource<HomeUserInformation> =
        safeApiCall(dispatchersProvider) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
            val response = homeService.getHome(today)
            val baby = response.baby
                ?: throw IllegalArgumentException("Baby should not be null")
            HomeUserInformation(
                userName = response.userName,
                babyInformation = babyToDomain(baby),
                todaySleepMinutes = response.todaySleepMinutes,
                expectedSleepMinutes = response.expectedSleepMinutes,
                activeSleepSession = response.activeSleepSession?.toDomain(),
            )
        }

    override fun observeActiveFeeding(): Flow<ActiveFeedingInfo?> =
        feedingLocalDataSource.observeActiveSession().map { snapshot ->
            snapshot?.let {
                ActiveFeedingInfo(
                    activeBreast = it.activeBreast,
                    closedSegmentsTotalMs = it.closedSegmentsTotalMs,
                    activeSegmentStartedAt = it.activeSegmentStartedAt,
                )
            }
        }

    override suspend fun closeSleepSession(
        babyId: String,
        sessionId: String,
        endTimeMs: Long,
    ): Resource<Unit> = safeApiCall(dispatchersProvider) {
        sleepApiService.endSession(babyId, sessionId, EndSleepSessionRequest(endTimeMs.toIso8601()))
        Unit
    }

    private fun Long.toIso8601(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ROOT).format(Date(this))

    private fun ActiveSleepSessionDto.toDomain() = ActiveSleepSessionInfo(
        id = id.toString(),
        startTimeMs = parseIso8601ToMs(startTime),
        sleepType = sleepType,
    )

    private fun parseIso8601ToMs(iso: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.parse(iso)?.time ?: System.currentTimeMillis()
    }

    private fun babyToDomain(baby: BabyHomeDto) = HomeBabyInformation(
        babyId = baby.id.toString(),
        babyName = baby.name,
        babyBirthDate = baby.birthDate,
        babyPhotoUrl = baby.photoUrl,
        babyHeight = baby.birthHeight.setScale(SCALE_0, RoundingMode.HALF_UP),
        babyWeight = baby.birthWeight.setScale(SCALE_2, RoundingMode.HALF_UP),
    )

    companion object {
        private const val SCALE_0 = 0
        private const val SCALE_2 = 2
    }
}
