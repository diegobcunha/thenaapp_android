package com.diegocunha.thenaapp.feature.signup.domain

import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.UserResponse

interface SignupRepository {

    suspend fun createUser(email: String, password: String, name: String): Resource<UserResponse>

    suspend fun createUserWithGoogle(): Resource<GoogleSignUpResponse>

    suspend fun updateUser(name: String): Resource<UserResponse>
}
