package com.diegocunha.thenaapp.feature.login.presentation

import androidx.annotation.StringRes
import com.diegocunha.thenaapp.core.mvi.MviEffect

sealed interface LoginEffect : MviEffect {
    object NavigateToHome : LoginEffect
    object NavigateToSignUp : LoginEffect
    object NavigateToCreateBaby : LoginEffect
    data class NavigateToFinishRegistration(val hasBaby: Boolean, val isCompletionProfile: Boolean): LoginEffect
    data class ShowSnackbar(@StringRes val message: Int) : LoginEffect
}
