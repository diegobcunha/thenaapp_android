package com.diegocunha.thenaapp.datasource.network.interceptor

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class AccessTokenRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
) : AccessTokenRepository {

    override fun getAccessToken(): String? {
        val user = firebaseAuth.currentUser ?: return null

        return try {
            Tasks.await(
                user.getIdToken(false),
                10,
                TimeUnit.SECONDS
            ).token
        } catch (exception: Exception) {
            Log.e("AccessToken", "Failed to get token", exception)
            null
        }
    }
}