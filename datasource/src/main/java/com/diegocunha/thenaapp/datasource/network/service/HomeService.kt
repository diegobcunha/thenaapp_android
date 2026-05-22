package com.diegocunha.thenaapp.datasource.network.service

import com.diegocunha.thenaapp.datasource.network.model.home.HomeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeService {

    @GET("/v1/home")
    suspend fun getHome(@Query("date") date: String): HomeResponse
}
