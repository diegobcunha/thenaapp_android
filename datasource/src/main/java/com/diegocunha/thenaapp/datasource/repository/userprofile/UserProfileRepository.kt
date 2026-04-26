package com.diegocunha.thenaapp.datasource.repository.userprofile

import com.diegocunha.thenaapp.core.resource.Resource
import com.google.firebase.auth.FirebaseUser

interface UserProfileRepository {
    suspend fun getProfileStatus(): Resource<ProfileStatus>

    suspend fun getFirebaseAuthUser(): FirebaseUser
}
