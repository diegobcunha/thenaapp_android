package com.diegocunha.thenaapp

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
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.login.presentation.LoginScreen
import com.diegocunha.thenaapp.feature.login.presentation.LoginViewModel
import com.diegocunha.thenaapp.feature.login.presentation.navigation.LoginNavigation
import com.diegocunha.thenaapp.feature.signup.presentation.SignupScreen
import com.diegocunha.thenaapp.feature.signup.presentation.SignupViewModel
import com.diegocunha.thenaapp.feature.signup.presentation.navigation.SignupNavigation
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThenaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val backStack = rememberNavBackStack(LoginNavigation)
                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        popTransitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        entryProvider = entryProvider {
                            entry<LoginNavigation> {
                                LoginScreen(
                                    viewModel = koinViewModel<LoginViewModel>(),
                                    onNavigateToHome = {},
                                    onNavigateToSignUp = {
                                        backStack.add(SignupNavigation)
                                    }
                                )
                            }

                            entry<SignupNavigation> {
                                SignupScreen(
                                    viewModel = koinViewModel<SignupViewModel>(),
                                    onBackPressed = {
                                        backStack.removeLastOrNull()
                                    },
                                    navigateToOnBoarding = {}
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}