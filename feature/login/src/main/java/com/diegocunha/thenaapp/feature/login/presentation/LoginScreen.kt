package com.diegocunha.thenaapp.feature.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.login.R
import kotlinx.coroutines.flow.collectLatest
import com.diegocunha.thenaapp.coreui.R as CoreUiR

typealias LoginForm = Pair<Boolean, Boolean>
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToCreateBaby: () -> Unit,
    onNavigateToCompleteRegistration: (LoginForm) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> onNavigateToHome()
                is LoginEffect.NavigateToSignUp -> onNavigateToSignUp()
                is LoginEffect.NavigateToCreateBaby -> onNavigateToCreateBaby()
                is LoginEffect.NavigateToFinishRegistration -> onNavigateToCompleteRegistration(effect.hasBaby to effect.isCompletionProfile)
                is LoginEffect.ShowSnackbar -> snackbarHostState.showSnackbar(context.getString(effect.message))
            }
        }
    }

    val onEmailChange = remember(viewModel) { { email: String -> viewModel.sendIntent(LoginIntent.UpdateEmail(email)) } }
    val onPasswordChange = remember(viewModel) { { password: String -> viewModel.sendIntent(LoginIntent.UpdatePassword(password)) } }
    val onSubmitLogin = remember(viewModel) { { viewModel.sendIntent(LoginIntent.SubmitLogin) } }
    val onGoogleSignIn = remember(viewModel) { { viewModel.sendIntent(LoginIntent.TriggerGoogleSignIn) } }
    val onForgotPassword = remember(viewModel) { { viewModel.sendIntent(LoginIntent.ForgotPassword) } }
    val onSignUp = remember(viewModel) { { viewModel.sendIntent(LoginIntent.NavigateToSignUp) } }

    LoginScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEmailChange = onEmailChange,
        onPasswordChange = onPasswordChange,
        onSubmitLogin = onSubmitLogin,
        onGoogleSignIn = onGoogleSignIn,
        onForgotPassword = onForgotPassword,
        onSignUp = onSignUp,
    )
}

@Composable
private fun LoginScreenContent(
    state: LoginState,
    snackbarHostState: SnackbarHostState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmitLogin: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val spacing = ThenaTheme.spacing

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(colors.primaryContainer, colors.secondaryContainer)
                        )
                    )
                    .padding(
                        start = spacing.xl,
                        end = spacing.xl,
                        top = spacing.xxxl,
                        bottom = spacing.xxl,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(spacing.lg))
                            .background(colors.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "🌸", fontSize = 40.sp)
                    }
                    Spacer(Modifier.height(spacing.lg))
                    Text(
                        text = stringResource(R.string.login_app_name),
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(spacing.sm))
                    Text(
                        text = stringResource(R.string.login_app_tagline),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xl),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Text(
                    text = stringResource(R.string.login_welcome_back),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                )

                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(CoreUiR.string.login_email_label)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = state.emailError != null,
                    supportingText = state.emailError?.let { id -> { Text(stringResource(id)) } },
                    singleLine = true,
                )

                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(CoreUiR.string.login_password_label)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = stringResource(
                                    if (passwordVisible) CoreUiR.string.login_hide_password else CoreUiR.string.login_show_password
                                ),
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = state.passwordError != null,
                    supportingText = state.passwordError?.let { id -> { Text(stringResource(id)) } },
                    singleLine = true,
                )

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = onForgotPassword) {
                        Text(stringResource(R.string.login_forgot_password))
                    }
                }

                if (state.generalError != null) {
                    Text(
                        text = stringResource(state.generalError),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.error,
                    )
                }

                Button(
                    onClick = onSubmitLogin,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(spacing.m),
                            strokeWidth = ThenaTheme.spacing.xxs,
                            color = colors.onPrimary,
                        )
                    } else {
                        Text(stringResource(CoreUiR.string.login_sign_in))
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "  ${stringResource(CoreUiR.string.login_or_continue_with)}  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                OutlinedButton(
                    onClick = onGoogleSignIn,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                ) {
                    Text(
                        text = "G",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.primary,
                    )
                    Spacer(Modifier.width(spacing.md - spacing.xs))
                    Text(stringResource(CoreUiR.string.login_google_button))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.login_no_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                    TextButton(onClick = onSignUp) {
                        Text(stringResource(R.string.login_sign_up))
                    }
                }
            }
        }
    }
}
