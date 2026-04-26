package com.diegocunha.thenaapp.datasource.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(
    private val accessTokenRepository: AccessTokenRepository,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .header("Authorization", "Bearer ${accessTokenRepository.getAccessToken()}")
            .build()
        return chain.proceed(request)
    }
}