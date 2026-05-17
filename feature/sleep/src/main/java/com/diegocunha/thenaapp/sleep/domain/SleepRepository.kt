package com.diegocunha.thenaapp.sleep.domain

import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.sleep.domain.model.ActiveSleepSession
import com.diegocunha.thenaapp.sleep.domain.model.NextNapSuggestion
import com.diegocunha.thenaapp.sleep.domain.model.SleepDailyStats
import com.diegocunha.thenaapp.sleep.domain.model.SleepSchedule
import com.diegocunha.thenaapp.sleep.domain.model.SleepSession
import com.diegocunha.thenaapp.sleep.domain.model.SleepType
import com.diegocunha.thenaapp.sleep.domain.model.SleepWeeklyStats

interface SleepRepository {
    suspend fun getActiveSession(babyId: String): ActiveSleepSession?
    suspend fun startSession(babyId: String, sleepType: SleepType, startTimeMs: Long): Resource<SleepSession>
    suspend fun endSession(babyId: String, sessionId: String, endTimeMs: Long): Resource<SleepSession>
    suspend fun logPastSession(babyId: String, sleepType: SleepType, startTimeMs: Long, endTimeMs: Long): Resource<SleepSession>
    suspend fun listSessions(babyId: String, date: String): Resource<List<SleepSession>>
    suspend fun getDailyStats(babyId: String, date: String): Resource<SleepDailyStats>
    suspend fun getWeeklyStats(babyId: String, weekStart: String): Resource<SleepWeeklyStats>
    suspend fun getNextNap(babyId: String): Resource<NextNapSuggestion>
    suspend fun getSchedule(babyId: String): Resource<SleepSchedule>
}
