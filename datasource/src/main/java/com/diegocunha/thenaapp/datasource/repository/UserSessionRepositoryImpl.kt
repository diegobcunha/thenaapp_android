package com.diegocunha.thenaapp.datasource.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.datasource.storage.sharedpreferences.CustomSharedPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

internal class UserSessionRepositoryImpl(
    private val preferences: CustomSharedPreferences,
    private val firebaseAuth: FirebaseAuth,
    private val dispatchersProvider: DispatchersProvider,
) : UserSessionRepository {

    override suspend fun saveUserId(id: String) {
        preferences.putString(KEY_USER_ID, id)
    }

    override suspend fun getUserId(): String? {
        return preferences.getString(KEY_USER_ID)
    }

    override suspend fun refreshToken() = withContext(dispatchersProvider.io()) {
        withTimeout(UPDATE_TOKEN_TIMEOUT) {
            firebaseAuth.currentUser?.getIdToken(true)?.await()
            Unit
        }
    }

    override suspend fun hasUser(): Boolean = withContext(dispatchersProvider.io()) {
        firebaseAuth.currentUser != null
    }

    companion object {
        private const val KEY_USER_ID = "user_session_id"
        private const val UPDATE_TOKEN_TIMEOUT = 5_000L
    }
}
