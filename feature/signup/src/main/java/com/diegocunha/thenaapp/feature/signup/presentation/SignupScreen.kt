package com.diegocunha.thenaapp.feature.signup.presentation

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.signup.R
import kotlinx.coroutines.flow.collectLatest
import com.diegocunha.thenaapp.coreui.R as CoreUiR

@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    onBackPressed: () -> Unit,
    navigateToCreateBaby: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is SignupEffect.NavigateToOnboarding -> navigateToCreateBaby.invoke()
            }
        }
    }

    SignupScreenContent(
        state = state,
        onBackPressed = onBackPressed,
        snackbarHostState = snackbarHostState,
        onNameChange = { viewModel.sendIntent(SignupIntent.UpdateName(it)) },
        onEmailChange = { viewModel.sendIntent(SignupIntent.UpdateEmail(it)) },
        onPasswordChange = { viewModel.sendIntent(SignupIntent.UpdatePassword(it)) },
        onConfirmPasswordChange = { viewModel.sendIntent(SignupIntent.UpdateConfirmPassword(it)) },
        onSubmitSignup = { viewModel.sendIntent(SignupIntent.SubmitSignup) },
        onGoogleSignIn = { viewModel.sendIntent(SignupIntent.TriggerGoogleSignIn) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignupScreenContent(
    state: SignupState,
    onBackPressed: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmitSignup: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val spacing = ThenaTheme.spacing
    val generalErrorMessage = state.generalError?.let { stringResource(it) }

    LaunchedEffect(state.generalError) {
        if (generalErrorMessage != null) {
            snackbarHostState.showSnackbar(generalErrorMessage)
        }
    }

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
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = spacing.md,
                            start = spacing.md,
                        )
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "return"
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = spacing.xl,
                                end = spacing.xl,
                                top = spacing.xxxl,
                                bottom = spacing.xxl,
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.feature_signup_header),
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.onPrimaryContainer,
                        )

                        Spacer(Modifier.height(spacing.sm))
                        Text(
                            text = stringResource(R.string.feature_signup_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xl),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                OutlinedTextField(
                    enabled = !state.isLoading,
                    value = state.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(CoreUiR.string.login_name_label)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { id -> { Text(stringResource(id)) } },
                    singleLine = true,
                )

                OutlinedTextField(
                    enabled = !state.isSignupByGoogle || !state.isLoading,
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

                if (!state.isSignupByGoogle) {
                    OutlinedTextField(
                        enabled = !state.isLoading,
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

                    OutlinedTextField(
                        enabled = !state.isLoading,
                        value = state.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(CoreUiR.string.confirm_password_label)) },
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
                        isError = state.confirmPasswordError != null,
                        supportingText = state.confirmPasswordError?.let { id ->
                            {
                                Text(
                                    stringResource(
                                        id
                                    )
                                )
                            }
                        },
                        singleLine = true,
                    )
                }

                Button(
                    onClick = onSubmitSignup,
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

                if (!state.isSignupByGoogle) {
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
                }
            }
        }
    }
}

@Preview
@Composable
private fun SignupScreenPreview() {

    SignupScreenContent(
        SignupState(),
        onBackPressed = {},
        snackbarHostState = remember { SnackbarHostState() },
        onNameChange = {},
        onSubmitSignup = {},
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onGoogleSignIn = {}
    )
}


