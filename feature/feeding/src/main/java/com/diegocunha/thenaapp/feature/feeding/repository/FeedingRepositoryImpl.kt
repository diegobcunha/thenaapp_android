package com.diegocunha.thenaapp.feature.feeding.repository

import com.diegocunha.thenaapp.datasource.database.dao.BreastSegmentDao
import com.diegocunha.thenaapp.datasource.database.dao.FeedingSessionDao
import com.diegocunha.thenaapp.datasource.database.entity.BreastSegmentEntity
import com.diegocunha.thenaapp.datasource.database.entity.FeedingSessionEntity
import com.diegocunha.thenaapp.feature.feeding.domain.FeedingRepository
import com.diegocunha.thenaapp.feature.feeding.domain.model.ActiveFeedingSession
import com.diegocunha.thenaapp.feature.feeding.domain.model.BottleType
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast
import com.diegocunha.thenaapp.feature.feeding.domain.model.BreastSegment
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingType
import kotlinx.coroutines.flow.first

class FeedingRepositoryImpl(
    private val sessionDao: FeedingSessionDao,
    private val segmentDao: BreastSegmentDao,
) : FeedingRepository {

    override suspend fun getActiveSession(): ActiveFeedingSession? {
        val session = sessionDao.observeActiveSession().first() ?: return null
        val segments = segmentDao.getBySession(session.id)
        val leftSegments = segments.filter { it.breast == Breast.LEFT.name }.map { it.toDomain() }
        val rightSegments = segments.filter { it.breast == Breast.RIGHT.name }.map { it.toDomain() }
        val activeSegment = segments.firstOrNull { it.endedAt == null }
        return ActiveFeedingSession(
            sessionId = session.id,
            type = FeedingType.valueOf(session.type),
            startedAt = session.startedAt,
            activeBreast = activeSegment?.breast?.let { Breast.valueOf(it) },
            leftSegments = leftSegments,
            rightSegments = rightSegments,
        )
    }

    override suspend fun createBreastSession(sessionId: String, babyId: String, startedAt: Long) {
        sessionDao.insert(
            FeedingSessionEntity(
                id = sessionId,
                babyId = babyId,
                type = FeedingType.BREAST.name,
                startedAt = startedAt,
            )
        )
    }

    override suspend fun createBottleSession(
        sessionId: String,
        babyId: String,
        startedAt: Long,
        ml: Int,
        bottleType: BottleType,
    ) {
        sessionDao.insert(
            FeedingSessionEntity(
                id = sessionId,
                babyId = babyId,
                type = FeedingType.BOTTLE.name,
                startedAt = startedAt,
                endedAt = System.currentTimeMillis(),
                bottleMl = ml,
                bottleType = bottleType.name,
            )
        )
    }

    override suspend fun closeSession(sessionId: String, endedAt: Long) {
        val entity = sessionDao.getById(sessionId) ?: return
        sessionDao.update(entity.copy(endedAt = endedAt))
    }

    override suspend fun createSegment(
        segmentId: String,
        sessionId: String,
        breast: Breast,
        startedAt: Long,
    ) {
        segmentDao.insert(
            BreastSegmentEntity(
                id = segmentId,
                sessionId = sessionId,
                breast = breast.name,
                startedAt = startedAt,
            )
        )
    }

    override suspend fun closeSegment(segmentId: String, endedAt: Long) {
        val segment = segmentDao.getById(segmentId) ?: return
        segmentDao.update(segment.copy(endedAt = endedAt))
    }

    override suspend fun getActiveSegmentId(sessionId: String): String? =
        segmentDao.getActiveSegment(sessionId)?.id

    private fun BreastSegmentEntity.toDomain() = BreastSegment(
        id = id,
        breast = Breast.valueOf(breast),
        startedAt = startedAt,
        endedAt = endedAt,
    )
}
