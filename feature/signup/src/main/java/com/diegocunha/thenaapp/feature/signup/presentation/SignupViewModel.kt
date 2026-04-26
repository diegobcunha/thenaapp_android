package com.diegocunha.thenaapp.feature.signup.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.diegocunha.thenaapp.core.mvi.BaseViewModel
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.feature.signup.R
import com.diegocunha.thenaapp.feature.signup.domain.SignupRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import com.diegocunha.thenaapp.coreui.R as CoreUiR

class SignupViewModel(
    private val signupRepository: SignupRepository,
    isProfileCompletion: Boolean = false,
) : BaseViewModel<SignupState, SignupIntent, SignupEffect>(SignupState(isNameOnlyMode = isProfileCompletion)) {

    init {
        validateProfileCompletion(isProfileCompletion)
    }

    override fun processIntent(intent: SignupIntent) {
        when (intent) {
            is SignupIntent.UpdateName -> {
                updateState {
                    copy(
                        name = intent.name,
                        generalError = null,
                        nameError = null,
                        passwordError = null,
                        emailError = null,
                        confirmPasswordError = null
                    )
                }
            }

            is SignupIntent.UpdateEmail -> {
                updateState {
                    copy(
                        email = intent.email,
                        generalError = null,
                        nameError = null,
                        passwordError = null,
                        emailError = null,
                        confirmPasswordError = null
                    )
                }
            }

            is SignupIntent.SubmitSignup -> performSignUp()
            is SignupIntent.TriggerGoogleSignIn -> performSignUpWithGoogle()
            is SignupIntent.UpdatePassword -> {
                updateState {
                    copy(
                        password = intent.password,
                        generalError = null,
                        nameError = null,
                        passwordError = null,
                        emailError = null,
                        confirmPasswordError = null
                    )
                }
            }

            is SignupIntent.UpdateConfirmPassword -> {
                updateState {
                    copy(
                        confirmPassword = intent.confirmPassword,
                        generalError = null,
                        nameError = null,
                        passwordError = null,
                        emailError = null,
                        confirmPasswordError = null
                    )
                }
            }
        }
    }

    private fun validateProfileCompletion(isProfileCompletion: Boolean) {
        if (!isProfileCompletion) return

        viewModelScope.launch {
            when (val result = signupRepository.getUserForCompletion()) {
                is Resource.Success -> updateState {
                    copy(
                        isNameOnlyMode = true,
                        isLoading = false,
                        email = result.data.email
                    )
                }

                is Resource.Error -> {
                    Timber.e(result.exception)
                    updateState {
                        copy(
                            isLoading = false,
                            nameError = validateName("")
                        )
                    }
                }

                else -> Unit
            }

        }
    }

    private fun performSignUp() {
        val current = state.value
        val emailError = validateEmail(current.email)
        val passwordError = validatePassword(current.password)
        val nameError = validateName(current.name)
        val confirmPasswordError =
            validateConfirmPassword(current.password, current.confirmPassword)

        if (current.isNameOnlyMode) {
            performUpdateProfile(current.name)
            return
        }

        if (emailError != null || passwordError != null || nameError != null || confirmPasswordError != null) {
            updateState {
                copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    nameError = nameError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        viewModelScope.launch {
            updateState {
                copy(
                    isLoading = true,
                    generalError = null,
                    nameError = null,
                    passwordError = null,
                    emailError = null,
                    confirmPasswordError = null
                )
            }

            when (signupRepository.createUser(current.email, current.password, current.name)) {
                is Resource.Success -> {
                    sendEffect(SignupEffect.NavigateToOnboarding)
                }

                is Resource.Error -> {
                    updateState {
                        copy(isLoading = false, generalError = CoreUiR.string.generic_error)
                    }
                }

                else -> Unit
            }
        }
    }

    private fun performUpdateProfile(name: String) {
        viewModelScope.launch {
            val nameError = validateName(name)
            if (nameError != null) {
                updateState {
                    copy(
                        nameError = nameError
                    )
                }
                return@launch
            }

            when (signupRepository.updateUser(name)) {
                is Resource.Success -> sendEffect(SignupEffect.NavigateToOnboarding)
                is Resource.Error -> updateState {
                    copy(
                        isLoading = false,
                        generalError = CoreUiR.string.generic_error
                    )
                }

                else -> Unit
            }
        }

    }

    private fun performSignUpWithGoogle() {
        viewModelScope.launch {
            updateState {
                copy(
                    isLoading = true,
                    generalError = null,
                    nameError = null,
                    passwordError = null,
                    emailError = null,
                    confirmPasswordError = null
                )
            }

            when (val result = signupRepository.createUserWithGoogle()) {
                is Resource.Success -> {
                    val content = result.data
                    updateState {

                        if (content.isUserAlreadyCreated) {
                            copy(
                                isLoading = false,
                                generalError = R.string.feature_signup_already_created
                            )
                        } else {
                            copy(
                                isLoading = false,
                                email = result.data.email,
                                isNameOnlyMode = true
                            )
                        }
                    }
                }

                is Resource.Error -> {
                    Timber.e(result.exception)
                    updateState {
                        copy(
                            isLoading = false,
                            generalError = CoreUiR.string.generic_error
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    @StringRes
    private fun validateEmail(email: String): Int? {
        val emailRegex = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
        return when {
            email.isEmpty() || email.isBlank() -> CoreUiR.string.login_error_email_required
            !emailRegex.matches(email) -> CoreUiR.string.login_error_email_invalid
            else -> null
        }
    }

    @StringRes
    private fun validatePassword(
        password: String,
    ): Int? = when {
        password.isEmpty() || password.isBlank() -> CoreUiR.string.login_error_password_required
        password.length < 8 -> CoreUiR.string.login_error_password_too_short
        !password.any { it.isUpperCase() } -> CoreUiR.string.login_error_password_no_uppercase
        !password.any { it.isDigit() } -> CoreUiR.string.login_error_password_no_digit
        !password.any { !it.isLetterOrDigit() } -> CoreUiR.string.login_error_password_no_special

        else -> null
    }

    @StringRes
    fun validateConfirmPassword(
        password: String,
        confirmPassword: String,
    ): Int? {
        val validationPassword = validatePassword(confirmPassword)
        return when {
            validationPassword != null -> validationPassword
            confirmPassword != password -> R.string.feature_signup_confirm_password_match_error
            else -> null
        }
    }


    @StringRes
    private fun validateName(name: String): Int? {
        return when {
            name.isBlank() || name.isEmpty() -> R.string.feature_signup_name_empty
            else -> null
        }
    }
}
