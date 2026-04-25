package com.diegocunha.thenaapp.feature.login.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.diegocunha.thenaapp.core.mvi.MviState

@Immutable
data class LoginState(
    val email: String = "",
    val password: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val isLoading: Boolean = false,
    @StringRes val generalError: Int? = null,
) : MviState