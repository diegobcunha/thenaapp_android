package com.diegocunha.thenaapp.datasource.repository.userprofile

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.safeApiCall
import com.diegocunha.thenaapp.datasource.network.service.UserService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserProfileRepositoryImpl(
    private val userService: UserService,
    private val dispatchersProvider: DispatchersProvider,
    private val firebaseAuth: FirebaseAuth,
) : UserProfileRepository {

    override suspend fun getProfileStatus(): Resource<ProfileStatus> =
        safeApiCall(dispatchersProvider) {
            val user = userService.getUsersInformation()
            when {
                user.name.isNullOrBlank() -> ProfileStatus.MissingName(hasBaby = user.babies.isNotEmpty())
                user.babies.isEmpty() -> ProfileStatus.MissingBaby
                else -> ProfileStatus.Complete
            }
        }

    override suspend fun getFirebaseAuthUser(): FirebaseUser =
        firebaseAuth.currentUser ?: error("No authenticated user")
}
