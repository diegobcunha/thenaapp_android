package com.diegocunha.thenaapp.datasource.network.service

import com.diegocunha.thenaapp.datasource.network.model.UserResponse
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserService {

    @GET("/v1/users/me")
    suspend fun getUsersInformation(): UserResponse

    @PUT("/v1/users/me")
    suspend fun updateProfile(): UserResponse

    @PUT("/v1/users/me/fcm-token")
    suspend fun updateFcmToken()
}