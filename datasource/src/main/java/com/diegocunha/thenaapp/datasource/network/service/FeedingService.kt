package com.diegocunha.thenaapp.datasource.network.service

import com.diegocunha.thenaapp.datasource.network.model.PageResponse
import com.diegocunha.thenaapp.datasource.network.model.feeding.BottleRequest
import com.diegocunha.thenaapp.datasource.network.model.feeding.BreastSideRequest
import com.diegocunha.thenaapp.datasource.network.model.feeding.FeedingSessionResponse
import com.diegocunha.thenaapp.datasource.network.model.feeding.FeedingStatisticsResponse
import com.diegocunha.thenaapp.datasource.network.model.feeding.UpdateBottleFeedingRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedingService {

    @POST("/v1/baby/{babyId}/feeding/breastfeeding")
    suspend fun startBreastfeeding(
        @Path("babyId") babyId: String,
        @Body request: BreastSideRequest,
    ): FeedingSessionResponse

    @PUT("/v1/baby/{babyId}/feeding/{sessionId}/breastfeeding/switch")
    suspend fun switchBreastSide(
        @Path("babyId") babyId: String,
        @Path("sessionId") sessionId: String,
        @Body request: BreastSideRequest,
    ): FeedingSessionResponse

    @POST("/v1/baby/{babyId}/feeding/{sessionId}/complete")
    suspend fun completeSession(
        @Path("babyId") babyId: String,
        @Path("sessionId") sessionId: String,
    ): FeedingSessionResponse

    @POST("/v1/baby/{babyId}/feeding/bottle")
    suspend fun recordBottleFeeding(
        @Path("babyId") babyId: String,
        @Body request: BottleRequest,
    ): FeedingSessionResponse

    @PUT("/v1/baby/{babyId}/feeding/{sessionId}/bottle")
    suspend fun updateBottleFeeding(
        @Path("babyId") babyId: String,
        @Path("sessionId") sessionId: String,
        @Body request: UpdateBottleFeedingRequest,
    ): FeedingSessionResponse

    @DELETE("/v1/baby/{babyId}/feeding/{sessionId}")
    suspend fun deleteFeeding(
        @Path("babyId") babyId: String,
        @Path("sessionId") sessionId: String,
    )

    @GET("/v1/baby/{babyId}/feeding/active")
    suspend fun getActiveSession(
        @Path("babyId") babyId: String,
    ): FeedingSessionResponse?

    @GET("/v1/baby/{babyId}/feeding/{sessionId}")
    suspend fun getSession(
        @Path("babyId") babyId: String,
        @Path("sessionId") sessionId: String,
    ): FeedingSessionResponse

    @GET("/v1/baby/{babyId}/feeding")
    suspend fun listSessions(
        @Path("babyId") babyId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("type") type: String? = null,
        @Query("date") date: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
    ): PageResponse<FeedingSessionResponse>

    @GET("/v1/baby/{babyId}/feeding/statistics")
    suspend fun getStatistics(
        @Path("babyId") babyId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): FeedingStatisticsResponse
}