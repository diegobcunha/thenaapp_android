package com.diegocunha.thenaapp.datasource.network.interceptor

interface AccessTokenRepository {

    fun getAccessToken(forceRefresh: Boolean = false): String?
}
