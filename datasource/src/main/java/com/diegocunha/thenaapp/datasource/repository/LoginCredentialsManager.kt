package com.diegocunha.thenaapp.datasource.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import timber.log.Timber

class LoginCredentialsManager(
    private val credentialManager: CredentialManager,
    private val context: Context,
) {

    suspend fun getGoogleIdToken(): String {
        val resId =
            context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                .run {
                    context.getString(this)
                }

        val signInOptions = GetSignInWithGoogleOption.Builder(resId)
            .build();

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOptions)
            .build()

        val result = try {
            credentialManager.getCredential(context, request)
        } catch (e: GetCredentialException) {
            Timber.e(e)
            throw GoogleSignInException("Google Sign-In failed", e)
        }

        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }

        throw GoogleSignInException("Unexpected credential type: ${credential.type}")
    }
}
