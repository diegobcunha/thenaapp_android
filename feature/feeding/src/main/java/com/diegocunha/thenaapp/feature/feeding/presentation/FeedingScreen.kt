package com.diegocunha.thenaapp.feature.feeding.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.feeding.R
import com.diegocunha.thenaapp.feature.feeding.domain.model.BottleType
import com.diegocunha.thenaapp.feature.feeding.domain.model.Breast
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingType
import com.diegocunha.thenaapp.feature.feeding.presentation.components.BottleFeedingCard
import com.diegocunha.thenaapp.feature.feeding.presentation.components.BreastfeedingTimerCard
import com.diegocunha.thenaapp.feature.feeding.presentation.components.FeedingTypePicker
import com.skydoves.compose.stability.runtime.TraceRecomposition
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@TraceRecomposition
@Composable
fun FeedingScreen(
    viewModel: FeedingViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val onFeedingBottle =
        remember(viewModel) { { viewModel.sendIntent(FeedingIntent.SelectBottle) } }
    val onFeedingBreast =
        remember(viewModel) { { viewModel.sendIntent(FeedingIntent.SelectBreastfeeding) } }
    val onFinish =
        remember(viewModel) { { viewModel.sendIntent(FeedingIntent.StopSession) } }
    val onTapBreast =
        remember(viewModel) {
            { breast: Breast ->
                viewModel.sendIntent(
                    FeedingIntent.TapBreast(
                        breast
                    )
                )
            }
        }
    val onMlChange =
        remember(viewModel) {
            { mlValue: String ->
                viewModel.sendIntent(
                    FeedingIntent.UpdateBottleMl(
                        mlValue
                    )
                )
            }
        }
    val onSave = remember(viewModel) { { viewModel.sendIntent(FeedingIntent.SaveBottleFeeding) } }
    val onTypeSelect =
        remember(viewModel) {
            { type: BottleType ->
                viewModel.sendIntent(
                    FeedingIntent.SelectBottleType(
                        type
                    )
                )
            }
        }



    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                FeedingEffect.NavigateBack -> onNavigateBack()
                is FeedingEffect.ShowError -> snackbarHostState.showSnackbar(effect.message.toString())
            }
        }
    }

    FeedingScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onFeedingBottle = onFeedingBottle,
        onFeedingBreast = onFeedingBreast,
        onFinish = onFinish,
        onTapBreast = onTapBreast,
        onMlChange = onMlChange,
        onSave = onSave,
        onTypeSelect = onTypeSelect,
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedingScreenContent(
    state: FeedingState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onFeedingBottle: () -> Unit,
    onFeedingBreast: () -> Unit,
    onFinish: () -> Unit,
    onTapBreast: (Breast) -> Unit,
    onMlChange: (String) -> Unit,
    onSave: () -> Unit,
    onTypeSelect: (BottleType) -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feeding_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ThenaTheme.spacing.lg, vertical = ThenaTheme.spacing.md),
        ) {
            FeedingTypePicker(
                selected = state.feedingType,
                onSelect = { type ->
                    when (type) {
                        FeedingType.BREAST -> onFeedingBreast()
                        FeedingType.BOTTLE -> onFeedingBottle()
                    }
                },
            )

            Spacer(modifier = Modifier.height(ThenaTheme.spacing.md))

            when (state.feedingType) {
                null, FeedingType.BREAST -> BreastfeedingTimerCard(
                    state = state,
                    onTapBreast = onTapBreast,
                    onFinish = onFinish,
                )

                FeedingType.BOTTLE -> BottleFeedingCard(
                    state = state,
                    onMlChange = onMlChange,
                    onTypeSelect = onTypeSelect,
                    onSave = onSave,
                )
            }
        }
    }
}
