package com.diegocunha.thenaapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.diegocunha.thenaapp.core.resource.Resource
import com.diegocunha.thenaapp.datasource.repository.UserSessionRepository
import com.diegocunha.thenaapp.datasource.repository.userprofile.ProfileStatus
import com.diegocunha.thenaapp.datasource.repository.userprofile.UserProfileRepository
import com.diegocunha.thenaapp.feature.baby.presentation.create.navigation.CreateBabyNavigation
import com.diegocunha.thenaapp.feature.home.presentation.navigation.HomeNavigation
import com.diegocunha.thenaapp.feature.login.presentation.navigation.LoginNavigation
import com.diegocunha.thenaapp.feature.onboarding.domain.repository.OnboardingRepository
import com.diegocunha.thenaapp.feature.onboarding.presentation.navigation.OnboardingNavigation
import com.diegocunha.thenaapp.feature.signup.presentation.navigation.SignupNavigation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

class MainViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val userSessionRepository: UserSessionRepository,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    val startDestination: StateFlow<NavKey?> = flow {
        val hasOnboarding = !onboardingRepository.hasSeenOnboarding()
        val navigation: NavKey = when {
            hasOnboarding -> OnboardingNavigation
            !userSessionRepository.hasUser() -> LoginNavigation
            else -> try {
                userSessionRepository.refreshToken()
                when (val result = userProfileRepository.getProfileStatus()) {
                    is Resource.Success -> profileStatusToDestination(result.data)
                    else -> LoginNavigation
                }
            } catch (ex: Exception) {
                Timber.e(ex)
                LoginNavigation
            }
        }
        emit(navigation)
    }.catch { Timber.e(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    private fun profileStatusToDestination(status: ProfileStatus): NavKey = when (status) {
        is ProfileStatus.MissingName -> SignupNavigation(
            isProfileCompletion = true,
            hasBaby = status.hasBaby,
        )

        ProfileStatus.MissingBaby -> CreateBabyNavigation
        ProfileStatus.Complete -> HomeNavigation
    }
}
