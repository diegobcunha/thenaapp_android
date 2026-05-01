package com.diegocunha.thenaapp.feature.feeding.domain

import com.diegocunha.thenaapp.feature.feeding.domain.model.ActiveFeedingSession
import com.diegocunha.thenaapp.feature.feeding.domain.model.BottleType
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast

interface FeedingRepository {
    suspend fun getActiveSession(): ActiveFeedingSession?
    suspend fun createBreastSession(sessionId: String, babyId: String, startedAt: Long)
    suspend fun createBottleSession(sessionId: String, babyId: String, startedAt: Long, ml: Int, bottleType: BottleType)
    suspend fun closeSession(sessionId: String, endedAt: Long)
    suspend fun createSegment(segmentId: String, sessionId: String, breast: Breast, startedAt: Long)
    suspend fun closeSegment(segmentId: String, endedAt: Long)
    suspend fun getActiveSegmentId(sessionId: String): String?
}
