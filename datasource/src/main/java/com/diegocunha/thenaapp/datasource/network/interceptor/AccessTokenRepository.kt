package com.diegocunha.thenaapp.datasource.network.interceptor

interface AccessTokenRepository {

    fun getAccessToken(): String?
}