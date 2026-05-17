package com.diegocunha.thenaapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.baby.presentation.create.CreateBabyScreen
import com.diegocunha.thenaapp.feature.baby.presentation.create.CreateBabyViewModel
import com.diegocunha.thenaapp.feature.baby.presentation.create.navigation.CreateBabyNavigation
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingScreen
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingStatisticsScreen
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingStatisticsViewModel
import com.diegocunha.thenaapp.feature.feeding.presentation.FeedingViewModel
import com.diegocunha.thenaapp.feature.feeding.presentation.navigation.FeedingNavigation
import com.diegocunha.thenaapp.feature.feeding.presentation.navigation.FeedingStatisticsNavigation
import com.diegocunha.thenaapp.feature.home.presentation.HomeScreen
import com.diegocunha.thenaapp.feature.home.presentation.HomeViewModel
import com.diegocunha.thenaapp.feature.home.presentation.navigation.HomeNavigation
import com.diegocunha.thenaapp.feature.login.presentation.LoginScreen
import com.diegocunha.thenaapp.feature.login.presentation.LoginViewModel
import com.diegocunha.thenaapp.feature.login.presentation.navigation.LoginNavigation
import com.diegocunha.thenaapp.feature.onboarding.presentation.OnboardingScreen
import com.diegocunha.thenaapp.feature.onboarding.presentation.OnboardingViewModel
import com.diegocunha.thenaapp.feature.onboarding.presentation.navigation.OnboardingNavigation
import com.diegocunha.thenaapp.feature.signup.presentation.SignupScreen
import com.diegocunha.thenaapp.feature.signup.presentation.SignupViewModel
import com.diegocunha.thenaapp.feature.signup.presentation.navigation.SignupNavigation
import com.diegocunha.thenaapp.sleep.presentation.SleepScreen
import com.diegocunha.thenaapp.sleep.presentation.SleepStatisticsScreen
import com.diegocunha.thenaapp.sleep.presentation.SleepStatisticsViewModel
import com.diegocunha.thenaapp.sleep.presentation.SleepViewModel
import com.diegocunha.thenaapp.sleep.presentation.navigation.SleepNavigation
import com.diegocunha.thenaapp.sleep.presentation.navigation.SleepStatisticsNavigation
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()
        setContent {
            ThenaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()
                    val destination = startDestination
                    if (destination == null) {
                        SplashScreen()
                    } else {
                        val backStack = rememberNavBackStack(destination)
                        NavDisplay(
                            backStack = backStack,
                            onBack = { backStack.removeLastOrNull() },
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator()
                            ),
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
                                    animationSpec = tween(300)
                                )
                            },
                            popTransitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
                                    animationSpec = tween(300)
                                )
                            },
                            entryProvider = entryProvider {
                                entry<OnboardingNavigation> {
                                    OnboardingScreen(
                                        viewModel = koinViewModel<OnboardingViewModel>(),
                                        onNavigateToLogin = {
                                            backStack.removeLastOrNull()
                                            backStack.add(LoginNavigation)
                                        }
                                    )
                                }

                                entry<LoginNavigation> {
                                    LoginScreen(
                                        viewModel = koinViewModel<LoginViewModel>(),
                                        onNavigateToHome = {
                                            with(backStack) {
                                                removeLastOrNull()
                                                add(HomeNavigation)
                                            }
                                        },
                                        onNavigateToSignUp = {
                                            backStack.add(SignupNavigation())
                                        },
                                        onNavigateToCreateBaby = {
                                            backStack.removeLastOrNull()
                                            backStack.add(CreateBabyNavigation)
                                        },
                                        {
                                            with(backStack) {
                                                removeLastOrNull()
                                                backStack.add(
                                                    SignupNavigation(
                                                        hasBaby = it.first,
                                                        isProfileCompletion = it.second
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }

                                entry<SignupNavigation> { key ->
                                    SignupScreen(
                                        viewModel = koinViewModel<SignupViewModel>(
                                            parameters = { parametersOf(key.isProfileCompletion) }
                                        ),
                                        onBackPressed = {
                                            backStack.removeLastOrNull()
                                        },
                                        navigateAfterSignup = {
                                            with(backStack) {
                                                clear()
                                                add(if (key.hasBaby) HomeNavigation else CreateBabyNavigation)
                                            }
                                        }
                                    )
                                }

                                entry<CreateBabyNavigation> {
                                    CreateBabyScreen(
                                        viewModel = koinViewModel<CreateBabyViewModel>(),
                                        onNavigateToHome = {
                                            with(backStack) {
                                                removeLastOrNull()
                                                add(HomeNavigation)
                                            }
                                        },
                                        onNavigateBack = { backStack.removeLastOrNull() },
                                    )
                                }

                                entry<HomeNavigation> {
                                    HomeScreen(
                                        viewModel = koinViewModel<HomeViewModel>(),
                                        onNavigateToFeeding = { babyId -> backStack.add(FeedingNavigation(babyId)) },
                                        onNavigateToSleep = { babyId -> backStack.add(SleepNavigation(babyId)) },
                                    )
                                }

                                entry<FeedingNavigation> { key ->
                                    FeedingScreen(
                                        viewModel = koinViewModel<FeedingViewModel>(
                                            parameters = { parametersOf(key.babyId) }
                                        ),
                                        onNavigateBack = { backStack.removeLastOrNull() },
                                        onNavigateToStatistics = {
                                            backStack.add(FeedingStatisticsNavigation(key.babyId))
                                        },
                                    )
                                }

                                entry<FeedingStatisticsNavigation> { key ->
                                    FeedingStatisticsScreen(
                                        viewModel = koinViewModel<FeedingStatisticsViewModel>(
                                            parameters = { parametersOf(key.babyId) }
                                        ),
                                        onNavigateBack = { backStack.removeLastOrNull() },
                                    )
                                }

                                entry<SleepNavigation> { key ->
                                    SleepScreen(
                                        viewModel = koinViewModel<SleepViewModel>(
                                            parameters = { parametersOf(key.babyId) }
                                        ),
                                        onNavigateBack = { backStack.removeLastOrNull() },
                                        onNavigateToStatistics = {
                                            backStack.add(SleepStatisticsNavigation(key.babyId))
                                        },
                                    )
                                }

                                entry<SleepStatisticsNavigation> { key ->
                                    SleepStatisticsScreen(
                                        viewModel = koinViewModel<SleepStatisticsViewModel>(
                                            parameters = { parametersOf(key.babyId) }
                                        ),
                                        onNavigateBack = { backStack.removeLastOrNull() },
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
