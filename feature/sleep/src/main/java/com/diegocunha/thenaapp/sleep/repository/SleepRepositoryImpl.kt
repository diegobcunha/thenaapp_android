package com.diegocunha.thenaapp.sleep.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.sleep.EndSleepSessionRequest
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepActiveSessionResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepDailyStatsResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepScheduleResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepSessionResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepTypeResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepWeeklyStatsResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.StartSleepSessionRequest
import com.diegocunha.thenaapp.datasource.network.safeApiCall
import com.diegocunha.thenaapp.datasource.network.service.SleepApiService
import com.diegocunha.thenaapp.sleep.domain.SleepRepository
import com.diegocunha.thenaapp.sleep.domain.model.ActiveSleepSession
import com.diegocunha.thenaapp.sleep.domain.model.NapScheduleItem
import com.diegocunha.thenaapp.sleep.domain.model.NextNapSuggestion
import com.diegocunha.thenaapp.sleep.domain.model.SleepDailyStats
import com.diegocunha.thenaapp.sleep.domain.model.SleepSchedule
import com.diegocunha.thenaapp.sleep.domain.model.SleepSession
import com.diegocunha.thenaapp.sleep.domain.model.SleepType
import com.diegocunha.thenaapp.sleep.domain.model.SleepWeeklyStats
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SleepRepositoryImpl(
    private val sleepApiService: SleepApiService,
    private val dispatchersProvider: DispatchersProvider,
) : SleepRepository {

    override suspend fun getActiveSession(babyId: String): ActiveSleepSession? =
        withContext(dispatchersProvider.io()) {
            runCatching { sleepApiService.getActiveSession(babyId) }
                .getOrNull()
                ?.toDomain()
        }

    override suspend fun startSession(
        babyId: String,
        sleepType: SleepType,
        startTimeMs: Long,
    ): Resource<SleepSession> = safeApiCall(dispatchersProvider) {
        val request = StartSleepSessionRequest(
            startTime = startTimeMs.toIso8601(),
            timezone = TimeZone.getDefault().id,
            sleepType = sleepType.toResponse(),
        )
        sleepApiService.startSession(babyId, request).toDomain()
    }

    override suspend fun endSession(
        babyId: String,
        sessionId: String,
        endTimeMs: Long,
    ): Resource<SleepSession> = safeApiCall(dispatchersProvider) {
        sleepApiService.endSession(babyId, sessionId, EndSleepSessionRequest(endTimeMs.toIso8601())).toDomain()
    }

    override suspend fun logPastSession(
        babyId: String,
        sleepType: SleepType,
        startTimeMs: Long,
        endTimeMs: Long,
    ): Resource<SleepSession> = safeApiCall(dispatchersProvider) {
        val request = StartSleepSessionRequest(
            startTime = startTimeMs.toIso8601(),
            endTime = endTimeMs.toIso8601(),
            timezone = TimeZone.getDefault().id,
            sleepType = sleepType.toResponse(),
        )
        sleepApiService.startSession(babyId, request).toDomain()
    }

    override suspend fun listSessions(babyId: String, date: String): Resource<List<SleepSession>> =
        safeApiCall(dispatchersProvider) {
            sleepApiService.listSessions(babyId, date).map { it.toDomain() }
        }

    override suspend fun getDailyStats(babyId: String, date: String): Resource<SleepDailyStats> =
        safeApiCall(dispatchersProvider) {
            sleepApiService.getDailyStats(babyId, date).toDomain()
        }

    override suspend fun getWeeklyStats(babyId: String, weekStart: String): Resource<SleepWeeklyStats> =
        safeApiCall(dispatchersProvider) {
            sleepApiService.getWeeklyStats(babyId, weekStart).toDomain()
        }

    override suspend fun getNextNap(babyId: String): Resource<NextNapSuggestion> =
        safeApiCall(dispatchersProvider) {
            val response = sleepApiService.getNextNap(babyId)
            NextNapSuggestion(
                suggestedNapStart = response.suggestedNapStart,
                windowOpen = response.windowOpen,
                windowClose = response.windowClose,
                urgency = response.urgency,
                wakeWindowMinutes = response.wakeWindowMinutes,
            )
        }

    override suspend fun getSchedule(babyId: String): Resource<SleepSchedule> =
        safeApiCall(dispatchersProvider) {
            sleepApiService.getSchedule(babyId).toDomain()
        }

    private fun SleepActiveSessionResponse.toDomain() = ActiveSleepSession(
        id = id,
        startTimeMs = parseIso8601(startTime),
        sleepType = sleepType.toDomain(),
    )

    private fun SleepSessionResponse.toDomain() = SleepSession(
        id = id,
        startTimeMs = parseIso8601(startTime),
        endTimeMs = endTime?.let { parseIso8601(it) },
        durationMinutes = durationMinutes,
        sleepType = sleepType.toDomain(),
        isActive = isActive,
    )

    private fun SleepDailyStatsResponse.toDomain() = SleepDailyStats(
        date = date,
        totalSleepMinutes = totalSleepMinutes,
        sessionCount = sessionCount,
        napMinutes = napMinutes ?: 0L,
        nightSleepMinutes = nightSleepMinutes ?: 0L,
        efficiency = (efficiencyPercent ?: 0f) / 100f,
        goalMinutes = 0,
        insight = insight,
    )

    private fun SleepWeeklyStatsResponse.toDomain() = SleepWeeklyStats(
        days = days.map { it.toDomain() }.toPersistentList(),
        weeklyAvgMinutes = weeklyAvgMinutes,
        trend = trend,
    )

    private fun SleepScheduleResponse.toDomain() = SleepSchedule(
        date = date,
        naps = naps.map { NapScheduleItem(it.startTime, it.durationMinutes, it.sleepType.toDomain()) }.toPersistentList(),
        nightSleepMinutes = nightSleepMinutes,
        totalSleepMinutes = totalSleepMinutes,
    )

    private fun SleepTypeResponse.toDomain(): SleepType = when (this) {
        SleepTypeResponse.NAP -> SleepType.NAP
        SleepTypeResponse.NIGHT_SLEEP -> SleepType.NIGHT_SLEEP
        SleepTypeResponse.EARLY_MORNING -> SleepType.EARLY_MORNING
        SleepTypeResponse.CATNAP -> SleepType.CATNAP
        SleepTypeResponse.CONTACT_NAP -> SleepType.CONTACT_NAP
        SleepTypeResponse.CAR_NAP -> SleepType.CAR_NAP
    }

    private fun SleepType.toResponse(): SleepTypeResponse = when (this) {
        SleepType.NAP -> SleepTypeResponse.NAP
        SleepType.NIGHT_SLEEP -> SleepTypeResponse.NIGHT_SLEEP
        SleepType.EARLY_MORNING -> SleepTypeResponse.EARLY_MORNING
        SleepType.CATNAP -> SleepTypeResponse.CATNAP
        SleepType.CONTACT_NAP -> SleepTypeResponse.CONTACT_NAP
        SleepType.CAR_NAP -> SleepTypeResponse.CAR_NAP
    }

    private fun Long.toIso8601(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ROOT)
        .format(Date(this))

    private fun parseIso8601(value: String): Long = runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ROOT).parse(value)?.time ?: 0L
    }.getOrElse { 0L }
}
