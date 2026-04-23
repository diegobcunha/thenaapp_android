package com.diegocunha.thenaapp.feature.signup.presentation

import com.diegocunha.thenaapp.core.mvi.MviIntent

sealed interface SignupIntent : MviIntent {

    data class UpdateName(val name: String) : SignupIntent
    data class UpdateEmail(val email: String) : SignupIntent
    data class UpdatePassword(val password: String) : SignupIntent

    data class UpdateConfirmPassword(val confirmPassword: String) : SignupIntent
    object SubmitSignup : SignupIntent
    object TriggerGoogleSignIn : SignupIntent
}
