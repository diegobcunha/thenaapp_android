package com.diegocunha.thenaapp.feature.login.domain

import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.UserResponse

interface LoginRepository {

    suspend fun performLogin(email: String, password: String): Resource<UserResponse>

    suspend fun loginWithGoogle(): Resource<UserInformation>

    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
}
