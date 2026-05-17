package com.diegocunha.thenaapp.feature.feeding.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.database.dao.BreastSegmentDao
import com.diegocunha.thenaapp.datasource.database.dao.FeedingSessionDao
import com.diegocunha.thenaapp.datasource.database.entity.BreastSegmentEntity
import com.diegocunha.thenaapp.datasource.database.entity.FeedingSessionEntity
import com.diegocunha.thenaapp.datasource.network.model.feeding.BottleRequest
import com.diegocunha.thenaapp.datasource.network.model.feeding.BreastSide
import com.diegocunha.thenaapp.datasource.network.model.feeding.BreastSideRequest
import com.diegocunha.thenaapp.datasource.network.model.feeding.DailyFeedingStatisticsResponse
import com.diegocunha.thenaapp.datasource.network.model.feeding.FeedingStatisticsResponse
import com.diegocunha.thenaapp.datasource.network.model.feeding.MilkType
import com.diegocunha.thenaapp.datasource.network.model.feeding.UpdateStartTimeRequest
import com.diegocunha.thenaapp.datasource.network.safeApiCall
import com.diegocunha.thenaapp.datasource.network.service.FeedingService
import com.diegocunha.thenaapp.feature.feeding.domain.FeedingRepository
import com.diegocunha.thenaapp.feature.feeding.domain.model.ActiveFeedingSession
import com.diegocunha.thenaapp.feature.feeding.domain.model.BottleType
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast
import com.diegocunha.thenaapp.feature.feeding.domain.model.BreastSegment
import com.diegocunha.thenaapp.feature.feeding.domain.model.DailyFeedingStatistics
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingStatistics
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class FeedingRepositoryImpl(
    private val sessionDao: FeedingSessionDao,
    private val segmentDao: BreastSegmentDao,
    private val feedingService: FeedingService,
    private val dispatchersProvider: DispatchersProvider,
) : FeedingRepository {

    override suspend fun getActiveSession(): ActiveFeedingSession? =
        withContext(dispatchersProvider.io()) {
            val session = sessionDao.observeActiveSession().first() ?: return@withContext null
            val segments = segmentDao.getBySession(session.id)
            val leftSegments =
                segments.filter { it.breast == Breast.LEFT.name }.map { it.toDomain() }
            val rightSegments =
                segments.filter { it.breast == Breast.RIGHT.name }.map { it.toDomain() }
            val activeSegment = segments.firstOrNull { it.endedAt == null }
            return@withContext ActiveFeedingSession(
                sessionId = session.id,
                type = FeedingType.valueOf(session.type),
                startedAt = session.startedAt,
                activeBreast = activeSegment?.breast?.let { Breast.valueOf(it) },
                leftSegments = leftSegments,
                rightSegments = rightSegments,
            )
        }

    override suspend fun createBreastSession(
        babyId: String,
        startedAt: Long,
        firstBreast: Breast
    ): Resource<String> = safeApiCall(dispatchersProvider) {
        val response =
            feedingService.startBreastfeeding(babyId, BreastSideRequest(firstBreast.toBreastSide()))
        val sessionId = response.id
        sessionDao.insert(
            FeedingSessionEntity(
                id = sessionId,
                babyId = babyId,
                type = FeedingType.BREAST.name,
                startedAt = startedAt,
            )
        )
        segmentDao.insert(
            BreastSegmentEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                breast = firstBreast.name,
                startedAt = startedAt,
            )
        )
        sessionId
    }

    override suspend fun createBottleSession(
        babyId: String,
        startedAt: Long,
        ml: Int,
        bottleType: BottleType
    ): Resource<Unit> = safeApiCall(dispatchersProvider) {
        val response = feedingService.recordBottleFeeding(
            babyId,
            BottleRequest(type = bottleType.toMilkType(), volume = BigDecimal(ml)),
        )
        sessionDao.insert(
            FeedingSessionEntity(
                id = response.id,
                babyId = babyId,
                type = FeedingType.BOTTLE.name,
                startedAt = startedAt,
                endedAt = System.currentTimeMillis(),
                bottleMl = ml,
                bottleType = bottleType.name,
            )
        )
    }

    override suspend fun closeSession(sessionId: String, endedAt: Long): Resource<Unit> =
        safeApiCall(dispatchersProvider) {
            val entity = sessionDao.getById(sessionId)
                ?: throw IllegalArgumentException("entity should not be null")
            sessionDao.update(entity.copy(endedAt = endedAt))
            feedingService.completeSession(entity.babyId, sessionId)
        }

    override suspend fun syncSwitchBreast(sessionId: String, newBreast: Breast): Resource<Unit> =
        safeApiCall(dispatchersProvider) {
            val babyId = sessionDao.getById(sessionId)?.babyId
                ?: throw IllegalArgumentException("Baby id should not be null")
            feedingService.switchBreastSide(
                babyId,
                sessionId,
                BreastSideRequest(newBreast.toBreastSide())
            )
        }

    override suspend fun createSegment(
        segmentId: String,
        sessionId: String,
        breast: Breast,
        startedAt: Long,
    ) = withContext(dispatchersProvider.io()) {
        segmentDao.insert(
            BreastSegmentEntity(
                id = segmentId,
                sessionId = sessionId,
                breast = breast.name,
                startedAt = startedAt,
            )
        )
    }

    override suspend fun closeSegment(segmentId: String, endedAt: Long) = safeApiCall(dispatchersProvider){
        val segment = segmentDao.getById(segmentId) ?: throw IllegalArgumentException("Segment shoult not be null")
        segmentDao.update(segment.copy(endedAt = endedAt))
    }

    override suspend fun getActiveSegmentId(sessionId: String): String? =
        segmentDao.getActiveSegment(sessionId)?.id

    override suspend fun updateSessionStartTime(sessionId: String, newStartedAt: Long): Resource<Unit> =
        safeApiCall(dispatchersProvider) {
            val entity = sessionDao.getById(sessionId)
                ?: throw IllegalArgumentException("Session not found")
            sessionDao.update(entity.copy(startedAt = newStartedAt))
            segmentDao.getBySession(sessionId)
                .minByOrNull { it.startedAt }
                ?.let { firstSegment -> segmentDao.update(firstSegment.copy(startedAt = newStartedAt)) }
            feedingService.updateSessionStartTime(
                entity.babyId,
                sessionId,
                UpdateStartTimeRequest(newStartedAt.toIso8601()),
            )
        }

    override suspend fun updateBreastStartTime(
        sessionId: String,
        breast: Breast,
        newStartedAt: Long,
    ): Resource<Unit> = safeApiCall(dispatchersProvider) {
        val now = System.currentTimeMillis()
        val allSegments = segmentDao.getBySession(sessionId)
        val breastSegments = allSegments.filter { it.breast == breast.name }
        val otherSegments = allSegments.filter { it.breast != breast.name }

        for (other in otherSegments) {
            val endAt = other.endedAt ?: now
            if (other.startedAt <= newStartedAt && newStartedAt < endAt) {
                throw IllegalArgumentException("overlap")
            }
        }

        if (breastSegments.isEmpty()) {
            val otherFirstStart = otherSegments.minByOrNull { it.startedAt }?.startedAt
                ?: throw IllegalStateException("No segments in session")
            if (newStartedAt >= otherFirstStart) {
                throw IllegalArgumentException("overlap")
            }
            segmentDao.insert(
                BreastSegmentEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    breast = breast.name,
                    startedAt = newStartedAt,
                    endedAt = otherFirstStart,
                )
            )
        } else {
            val firstSegment = breastSegments.minByOrNull { it.startedAt }!!
            val closedAt = firstSegment.endedAt
            if (closedAt != null && newStartedAt >= closedAt) {
                throw IllegalArgumentException("overlap")
            }
            segmentDao.update(firstSegment.copy(startedAt = newStartedAt))
        }

        val otherFirstStart = otherSegments.minByOrNull { it.startedAt }?.startedAt
        val newSessionStartedAt = if (otherFirstStart != null) {
            minOf(newStartedAt, otherFirstStart)
        } else {
            newStartedAt
        }

        val entity = sessionDao.getById(sessionId)
            ?: throw IllegalArgumentException("Session not found")
        sessionDao.update(entity.copy(startedAt = newSessionStartedAt))

        feedingService.updateSessionStartTime(
            entity.babyId,
            sessionId,
            UpdateStartTimeRequest(newSessionStartedAt.toIso8601()),
        )
    }

    override suspend fun getStatistics(
        babyId: String,
        date: String?,
        startDate: String?,
        endDate: String?,
    ): Resource<FeedingStatistics> = safeApiCall(dispatchersProvider) {
        feedingService.getStatistics(babyId, date, startDate, endDate).toDomain()
    }

    private fun FeedingStatisticsResponse.toDomain() = FeedingStatistics(
        periodStart = periodStart,
        periodEnd = periodEnd,
        totalSessions = totalSessions,
        breastfeedingSessions = breastfeedingSessions,
        bottleSessions = bottleSessions,
        totalBreastfeedingDurationSeconds = totalBreastfeedingDurationSeconds,
        averageBreastfeedingDurationSeconds = averageBreastfeedingDurationSeconds,
        totalBottleVolumeMl = totalBottleVolumeMl,
        averageBottleVolumeMl = averageBottleVolumeMl,
        volumeByMilkType = volumeByMilkType,
        dailyBreakdown = dailyBreakdown.map { it.toDomain() },
    )

    private fun DailyFeedingStatisticsResponse.toDomain() = DailyFeedingStatistics(
        date = date,
        totalSessions = totalSessions,
        breastfeedingSessions = breastfeedingSessions,
        bottleSessions = bottleSessions,
        totalBreastfeedingDurationSeconds = totalBreastfeedingDurationSeconds,
        totalBottleVolumeMl = totalBottleVolumeMl,
    )

    private fun BreastSegmentEntity.toDomain() = BreastSegment(
        id = id,
        breast = Breast.valueOf(breast),
        startedAt = startedAt,
        endedAt = endedAt,
    )

    private fun Breast.toBreastSide() = when (this) {
        Breast.LEFT -> BreastSide.LEFT
        Breast.RIGHT -> BreastSide.RIGHT
    }

    private fun BottleType.toMilkType() = when (this) {
        BottleType.MOTHERS_MILK -> MilkType.BREAST_MILK
        BottleType.POWDERED -> MilkType.POWDERED_MILK
    }

    private fun Long.toIso8601(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(this))
}
