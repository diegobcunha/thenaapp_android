package com.diegocunha.thenaapp.datasource.repository

import com.diegocunha.thenaapp.datasource.storage.sharedpreferences.CustomSharedPreferences

internal class UserSessionRepositoryImpl(
    private val preferences: CustomSharedPreferences,
) : UserSessionRepository {

    override suspend fun saveUserId(id: String) {
        preferences.putString(KEY_USER_ID, id)
    }

    override suspend fun getUserId(): String? {
        return preferences.getString(KEY_USER_ID)
    }

    companion object {
        private const val KEY_USER_ID = "user_session_id"
    }
}
