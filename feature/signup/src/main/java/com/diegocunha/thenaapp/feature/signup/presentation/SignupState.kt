package com.diegocunha.thenaapp.feature.signup.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.diegocunha.thenaapp.core.mvi.MviState

@Immutable
data class SignupState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSignupByGoogle: Boolean = false,
    val isLoading: Boolean = false,
    @StringRes val nameError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val generalError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
) : MviState {
}

