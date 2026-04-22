package com.diegocunha.thenaapp.feature.login.presentation

import com.diegocunha.thenaapp.core.mvi.MviIntent

sealed interface LoginIntent : MviIntent {
    data class UpdateEmail(val email: String) : LoginIntent
    data class UpdatePassword(val password: String) : LoginIntent
    object SubmitLogin : LoginIntent
    data class LoginWithGoogle(val idToken: String) : LoginIntent
    object TriggerGoogleSignIn : LoginIntent
    object ForgotPassword : LoginIntent
    object NavigateToSignUp : LoginIntent
}