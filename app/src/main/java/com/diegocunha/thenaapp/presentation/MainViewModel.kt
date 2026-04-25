package com.diegocunha.thenaapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.diegocunha.thenaapp.feature.home.presentation.navigation.HomeNavigation
import com.diegocunha.thenaapp.feature.login.presentation.navigation.LoginNavigation
import com.diegocunha.thenaapp.feature.onboarding.domain.repository.OnboardingRepository
import com.diegocunha.thenaapp.feature.onboarding.presentation.navigation.OnboardingNavigation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class MainViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    val startDestination: StateFlow<NavKey?> = flow {
        val hasOnboarding = !onboardingRepository.hasSeenOnboarding()
        val user = firebaseAuth.currentUser
        val navigation = when {
            hasOnboarding -> OnboardingNavigation
            user == null -> LoginNavigation
            else -> try {
                user.getIdToken(true).await()
                HomeNavigation
            } catch (ex: Exception) {
                Timber.e(ex)
                LoginNavigation
            }

        }
        emit(navigation)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)
}
