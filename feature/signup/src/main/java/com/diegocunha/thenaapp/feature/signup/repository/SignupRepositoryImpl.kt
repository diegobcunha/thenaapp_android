package com.diegocunha.thenaapp.feature.signup.repository

import com.diegocunha.thenaapp.core.coroutines.DispatchersProvider
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.network.model.PutUserRequest
import com.diegocunha.thenaapp.datasource.network.model.UserResponse
import com.diegocunha.thenaapp.datasource.network.safeApiCall
import com.diegocunha.thenaapp.datasource.network.service.UserService
import com.diegocunha.thenaapp.datasource.repository.GoogleSignInException
import com.diegocunha.thenaapp.datasource.repository.LoginCredentialsManager
import com.diegocunha.thenaapp.feature.signup.domain.GoogleSignUpResponse
import com.diegocunha.thenaapp.feature.signup.domain.SignupRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class SignupRepositoryImpl(
    private val userService: UserService,
    private val firebaseAuth: FirebaseAuth,
    private val dispatchersProvider: DispatchersProvider,
    private val loginCredentialsManager: LoginCredentialsManager,
) : SignupRepository {

    override suspend fun createUser(
        email: String,
        password: String,
        name: String
    ): Resource<UserResponse> = safeApiCall(dispatchersProvider) {
        val firebaseToken = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        if (firebaseToken.user == null) {
            throw Exception("User not created")
        }

        userService.updateProfile(PutUserRequest(name))
    }

    override suspend fun createUserWithGoogle(): Resource<GoogleSignUpResponse> =
        safeApiCall(dispatchersProvider) {
            val idToken = loginCredentialsManager.getGoogleIdToken()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
                ?: throw GoogleSignInException("User is null")
            val user = userService.getUsersInformation()
            GoogleSignUpResponse(email = user.email)
        }

    override suspend fun updateUser(name: String): Resource<UserResponse> =
        safeApiCall(dispatchersProvider) {
            userService.updateProfile(PutUserRequest(name))
        }
}
