package com.diegocunha.thenaapp.feature.login.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.login.R
import com.diegocunha.thenaapp.feature.login.domain.LoginRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRepository: LoginRepository,
) : BaseViewModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {

    override fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UpdateEmail -> updateState {
                copy(email = intent.email, emailError = null, generalError = null)
            }

            is LoginIntent.UpdatePassword -> updateState {
                copy(password = intent.password, passwordError = null, generalError = null)
            }

            is LoginIntent.SubmitLogin -> submitLogin()
            is LoginIntent.TriggerGoogleSignIn -> loginWithGoogle()
            is LoginIntent.ForgotPassword -> forgotPassword()
            is LoginIntent.NavigateToSignUp -> sendEffect(LoginEffect.NavigateToSignUp)
        }
    }

    private fun submitLogin() {
        val current = state.value
        val emailError = validateEmail(current.email)
        val passwordError = validatePassword(current.password)

        if (emailError != null || passwordError != null) {
            updateState { copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            updateState { copy(isLoading = true, generalError = null) }
            when (val result = loginRepository.performLogin(current.email, current.password)) {
                is Resource.Success -> sendEffect(LoginEffect.NavigateToHome)
                is Resource.Error -> updateState {
                    copy(isLoading = false, generalError = mapFirebaseError(result.exception))
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun loginWithGoogle() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, generalError = null) }
            when (val result = loginRepository.loginWithGoogle()) {
                is Resource.Success -> sendEffect(LoginEffect.NavigateToHome)
                is Resource.Error -> updateState {
                    copy(isLoading = false, generalError = mapFirebaseError(result.exception))
                }

                is Resource.Loading -> Unit
            }
        }
    }

    private fun forgotPassword() {
        val email = state.value.email
        val emailError = validateEmail(email)

        if (emailError != null) {
            updateState { copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            updateState { copy(isLoading = true, generalError = null) }
            when (val result = loginRepository.sendPasswordResetEmail(email)) {
                is Resource.Success -> {
                    updateState { copy(isLoading = false) }
                    sendEffect(LoginEffect.ShowSnackbar(R.string.login_password_reset_sent))
                }

                is Resource.Error -> updateState {
                    copy(isLoading = false, generalError = mapFirebaseError(result.exception))
                }

                is Resource.Loading -> Unit
            }
        }
    }

    @StringRes
    private fun validateEmail(email: String): Int? {
        if (email.isBlank()) return R.string.login_error_email_required
        val emailRegex = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(email)) return R.string.login_error_email_invalid
        return null
    }

    @StringRes
    private fun validatePassword(password: String): Int? {
        if (password.isBlank()) return R.string.login_error_password_required
        if (password.length < 8) return R.string.login_error_password_too_short
        if (!password.any { it.isUpperCase() }) return R.string.login_error_password_no_uppercase
        if (!password.any { it.isDigit() }) return R.string.login_error_password_no_digit
        if (!password.any { !it.isLetterOrDigit() }) return R.string.login_error_password_no_special
        return null
    }

    @StringRes
    private fun mapFirebaseError(exception: Throwable): Int = when (exception) {
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> R.string.login_error_invalid_credentials

        else -> R.string.login_error_generic
    }
}
