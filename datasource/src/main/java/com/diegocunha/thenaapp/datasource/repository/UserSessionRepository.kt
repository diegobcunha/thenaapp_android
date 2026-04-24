package com.diegocunha.thenaapp.datasource.repository

interface UserSessionRepository {
    suspend fun saveUserId(id: String)
    suspend fun getUserId(): String?
}