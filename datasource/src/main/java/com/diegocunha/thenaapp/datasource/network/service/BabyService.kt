package com.diegocunha.thenaapp.datasource.network.service

import com.diegocunha.thenaapp.datasource.network.model.BabyResponse
import com.diegocunha.thenaapp.datasource.network.model.CreateBabyRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface BabyService {

    @GET("/v1/baby/{id}")
    suspend fun getBabyInformation(@Path("id") id: UUID): BabyResponse

    @POST("/v1/baby")
    suspend fun createBaby(@Body createBabyRequest: CreateBabyRequest): BabyResponse

}
