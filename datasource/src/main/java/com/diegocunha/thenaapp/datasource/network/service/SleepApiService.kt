package com.diegocunha.thenaapp.datasource.network.service

import com.diegocunha.thenaapp.datasource.network.model.sleep.EndSleepSessionRequest
import com.diegocunha.thenaapp.datasource.network.model.sleep.NextNapSuggestionResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepActiveSessionResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepDailyStatsResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepScheduleResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepSessionResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.SleepWeeklyStatsResponse
import com.diegocunha.thenaapp.datasource.network.model.sleep.StartSleepSessionRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SleepApiService {

    @POST("/v1/baby/{babyId}/sleep/sessions")
    suspend fun startSession(
        @Path("babyId") babyId: String,
        @Body request: StartSleepSessionRequest,
    ): SleepSessionResponse

    @PATCH("/v1/baby/{babyId}/sleep/sessions/{sessionId}/end")
    suspend fun endSession(
        @Path("babyId") babyId: String,
        @Path("sessionId") sessionId: String,
        @Body request: EndSleepSessionRequest,
    ): SleepSessionResponse

    @GET("/v1/baby/{babyId}/sleep/sessions/active")
    suspend fun getActiveSession(
        @Path("babyId") babyId: String,
    ): SleepActiveSessionResponse?

    @GET("/v1/baby/{babyId}/sleep/sessions")
    suspend fun listSessions(
        @Path("babyId") babyId: String,
        @Query("date") date: String? = null,
    ): List<SleepSessionResponse>

    @GET("/v1/baby/{babyId}/sleep/stats/daily")
    suspend fun getDailyStats(
        @Path("babyId") babyId: String,
        @Query("date") date: String,
    ): SleepDailyStatsResponse

    @GET("/v1/baby/{babyId}/sleep/stats/weekly")
    suspend fun getWeeklyStats(
        @Path("babyId") babyId: String,
        @Query("weekStart") weekStart: String,
    ): SleepWeeklyStatsResponse

    @GET("/v1/baby/{babyId}/sleep/next-nap")
    suspend fun getNextNap(
        @Path("babyId") babyId: String,
    ): NextNapSuggestionResponse

    @GET("/v1/baby/{babyId}/sleep/schedule")
    suspend fun getSchedule(
        @Path("babyId") babyId: String,
    ): SleepScheduleResponse
}
