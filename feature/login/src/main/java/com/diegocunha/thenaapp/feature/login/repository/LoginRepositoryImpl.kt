package com.diegocunha.thenaapp.feature.login.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.UserResponse
import com.diegocunha.thenaapp.datasource.network.safeApiCall
import com.diegocunha.thenaapp.datasource.network.service.UserService
import com.diegocunha.thenaapp.feature.login.domain.LoginCredentialsManager
import com.diegocunha.thenaapp.feature.login.domain.LoginRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

internal class LoginRepositoryImpl(
    private val userService: UserService,
    private val dispatchersProvider: DispatchersProvider,
    private val firebaseAuth: FirebaseAuth,
    private val loginCredentialsManager: LoginCredentialsManager,
) : LoginRepository {

    override suspend fun performLogin(
        email: String,
        password: String,
    ): Resource<UserResponse> = safeApiCall(dispatchersProvider) {

        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        if (result.user == null) throw Exception("User not logged in")
        userService.getUsersInformation()
    }

    override suspend fun loginWithGoogle(): Resource<UserResponse> =
        safeApiCall(dispatchersProvider) {
            val idToken = loginCredentialsManager.getGoogleIdToken()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            if (result.user == null) throw Exception("Google Sign-In failed")
            userService.getUsersInformation()
        }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Unit> =
        withContext(dispatchersProvider.io()) {
            try {
                firebaseAuth.sendPasswordResetEmail(email).await()
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }
}