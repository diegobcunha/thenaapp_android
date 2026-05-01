package com.diegocunha.thenaapp.datasource.database

import com.diegocunha.thenaapp.datasource.database.dao.BreastSegmentDao
import com.diegocunha.thenaapp.datasource.database.dao.FeedingSessionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FeedingLocalDataSourceImpl(
    private val sessionDao: FeedingSessionDao,
    private val segmentDao: BreastSegmentDao,
) : FeedingLocalDataSource {

    override fun observeActiveSession(): Flow<ActiveFeedingSnapshot?> =
        sessionDao.observeActiveSession().flatMapLatest { session ->
            if (session == null) {
                flowOf(null)
            } else {
                segmentDao.observeBySession(session.id).map { segments ->
                    val activeSegment = segments.firstOrNull { it.endedAt == null }
                    ActiveFeedingSnapshot(
                        sessionId = session.id,
                        startedAt = session.startedAt,
                        activeBreast = activeSegment?.breast,
                        type = session.type,
                    )
                }
            }
        }
}