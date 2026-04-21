package com.diegocunha.thenaapp.datasource.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

fun createOkHttpClient(isDebug: Boolean): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(HeaderInterceptor())
        .addInterceptor(
            HttpLoggingInterceptor().setLevel(
                level =
                    if (isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            )
        )
        .build()
}
