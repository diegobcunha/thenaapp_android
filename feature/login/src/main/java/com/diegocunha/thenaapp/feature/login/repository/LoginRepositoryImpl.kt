package com.diegocunha.thenaapp.feature.login.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.UserResponse
import com.diegocunha.thenaapp.datasource.network.safeApiCall
import com.diegocunha.thenaapp.datasource.network.service.UserService
import com.diegocunha.thenaapp.feature.login.domain.LoginRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

internal class LoginRepositoryImpl(
    private val userService: UserService,
    private val dispatchersProvider: DispatchersProvider,
    private val firebaseAuth: FirebaseAuth,
) : LoginRepository {

    override suspend fun performLogin(
        email: String,
        password: String,
    ): Resource<UserResponse> = withContext(dispatchersProvider.io()) {
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            if (result.user == null) throw Exception("User not logged in")
            safeApiCall(dispatchersProvider) { userService.getUsersInformation() }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Resource<UserResponse> =
        withContext(dispatchersProvider.io()) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = firebaseAuth.signInWithCredential(credential).await()
                if (result.user == null) throw Exception("Google Sign-In failed")
                safeApiCall(dispatchersProvider) { userService.getUsersInformation() }
            } catch (e: Exception) {
                Resource.Error(e)
            }
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