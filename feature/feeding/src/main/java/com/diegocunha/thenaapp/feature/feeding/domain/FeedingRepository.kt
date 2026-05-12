package com.diegocunha.thenaapp.feature.feeding.domain

import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.feeding.domain.model.ActiveFeedingSession
import com.diegocunha.thenaapp.feature.feeding.domain.model.BottleType
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast

interface FeedingRepository {
    suspend fun getActiveSession(): ActiveFeedingSession?
    suspend fun createBreastSession(babyId: String, startedAt: Long, firstBreast: Breast): Resource<String>
    suspend fun createBottleSession(babyId: String, startedAt: Long, ml: Int, bottleType: BottleType): Resource<Unit>
    suspend fun closeSession(sessionId: String, endedAt: Long): Resource<Unit>
    suspend fun syncSwitchBreast(sessionId: String, newBreast: Breast): Resource<Unit>
    suspend fun createSegment(segmentId: String, sessionId: String, breast: Breast, startedAt: Long)
    suspend fun closeSegment(segmentId: String, endedAt: Long): Resource<Unit>
    suspend fun getActiveSegmentId(sessionId: String): String?
    suspend fun updateSessionStartTime(sessionId: String, newStartedAt: Long): Resource<Unit>
    suspend fun updateBreastStartTime(sessionId: String, breast: Breast, newStartedAt: Long): Resource<Unit>
}
