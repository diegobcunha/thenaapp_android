package com.diegocunha.thenaapp.feature.baby.presentation.create

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diegocunha.thenaapp.coreui.component.StepIndicator
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.baby.R
import com.diegocunha.thenaapp.feature.baby.domain.model.BabyGender
import com.diegocunha.thenaapp.feature.baby.domain.model.ResponsibleType
import com.diegocunha.thenaapp.feature.baby.presentation.create.components.AllSetStep
import com.diegocunha.thenaapp.feature.baby.presentation.create.components.BabyInfoStep
import com.diegocunha.thenaapp.feature.baby.presentation.create.components.BirthDetailsStep
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CreateBabyScreen(
    viewModel: CreateBabyViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is CreateBabyEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    val onBabyNameChange = remember(viewModel) { { name: String -> viewModel.sendIntent(CreateBabyIntent.OnNameChange(name)) } }
    val onPhotoBase64Change = remember(viewModel) { { uri: Uri? -> viewModel.sendIntent(CreateBabyIntent.OnPhotoChange(uri)) } }
    val onGenderChange = remember(viewModel) { { gender: BabyGender -> viewModel.sendIntent(CreateBabyIntent.OnGenderChange(gender)) } }
    val onResponsibleTypeChange = remember(viewModel) { { type: ResponsibleType -> viewModel.sendIntent(CreateBabyIntent.OnResponsibleTypeChange(type)) } }
    val onBirthDateChange = remember(viewModel) { { date: String -> viewModel.sendIntent(CreateBabyIntent.OnBirthDateChange(date)) } }
    val onBirthWeightChange = remember(viewModel) { { weight: String -> viewModel.sendIntent(CreateBabyIntent.OnWeightChange(weight)) } }
    val onBirthHeightChange = remember(viewModel) { { height: String -> viewModel.sendIntent(CreateBabyIntent.OnHeightChange(height)) } }
    val onNextPage = remember(viewModel) { { viewModel.sendIntent(CreateBabyIntent.OnNextPage) } }
    val onPreviousPage = remember(viewModel) { { viewModel.sendIntent(CreateBabyIntent.OnPreviousPage) } }
    val onCreateBaby = remember(viewModel) { { viewModel.sendIntent(CreateBabyIntent.CreateBaby) } }

    BackHandler(enabled = state.currentPage > 0) {
        viewModel.sendIntent(CreateBabyIntent.OnPreviousPage)
    }

    CreateBabyScreenContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onBabyNameChange = onBabyNameChange,
        onPhotoBase64Change = onPhotoBase64Change,
        onGenderChange = onGenderChange,
        onResponsibleTypeChange = onResponsibleTypeChange,
        onBirthDateChange = onBirthDateChange,
        onBirthWeightChange = onBirthWeightChange,
        onBirthHeightChange = onBirthHeightChange,
        onNextPage = onNextPage,
        onPreviousPage = onPreviousPage,
        onCreateBaby = onCreateBaby,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateBabyScreenContent(
    state: CreateBabyState,
    onNavigateBack: () -> Unit,
    onBabyNameChange: (String) -> Unit,
    onPhotoBase64Change: (Uri?) -> Unit,
    onGenderChange: (BabyGender) -> Unit,
    onResponsibleTypeChange: (ResponsibleType) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onBirthWeightChange: (String) -> Unit,
    onBirthHeightChange: (String) -> Unit,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onCreateBaby: () -> Unit,
) {
    val spacing = ThenaTheme.spacing
    val pagerState = rememberPagerState(pageCount = { 3 })

    LaunchedEffect(state.currentPage) {
        pagerState.animateScrollToPage(state.currentPage)
    }

    val stepLabels = listOf(
        stringResource(R.string.create_baby_step_info),
        stringResource(R.string.create_baby_step_birth),
        stringResource(R.string.create_baby_step_ready),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_baby_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.currentPage > 0) onPreviousPage() else onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            StepIndicator(
                currentPage = state.currentPage,
                labels = stepLabels.toImmutableList(),
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
            )
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> BabyInfoStep(
                        babyName = state.babyName,
                        babyNameError = state.babyNameError,
                        babyGender = state.babyGender,
                        babyGenderError = state.babyGenderError,
                        responsibleType = state.responsibleType,
                        responsibleTypeError = state.responsibleTypeError,
                        photo = state.photo,
                        onNameChange = onBabyNameChange,
                        onPhotoChange = onPhotoBase64Change,
                        onGenderChange = onGenderChange,
                        onResponsibleTypeChange = onResponsibleTypeChange,
                        onNext = onNextPage,
                    )

                    1 -> BirthDetailsStep(
                        birthDate = state.birthDate,
                        birthDateError = state.birthDateError,
                        birthWeight = state.birthWeight,
                        birthWeightError = state.birthWeightError,
                        birthHeight = state.birthHeight,
                        birthHeightError = state.birthHeightError,
                        onBirthDateChange = onBirthDateChange,
                        onBirthWeightChange = onBirthWeightChange,
                        onBirthHeightChange = onBirthHeightChange,
                        onBack = onPreviousPage,
                        onNext = onNextPage,
                    )

                    2 -> AllSetStep(
                        babyName = state.babyName,
                        isLoading = state.isLoading,
                        generalError = state.generalError,
                        onStartTracking = onCreateBaby,
                    )
                }
            }
        }
    }
}
