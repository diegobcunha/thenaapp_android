package com.diegocunha.thenaapp.sleep.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.sleep.R
import com.diegocunha.thenaapp.sleep.domain.model.SleepType
import com.diegocunha.thenaapp.sleep.presentation.components.LogSleepDialog
import com.diegocunha.thenaapp.sleep.presentation.components.SleepGoalCard
import com.diegocunha.thenaapp.sleep.presentation.components.SleepSessionItem
import com.diegocunha.thenaapp.sleep.presentation.components.SleepTimerCard
import com.skydoves.compose.stability.runtime.TraceRecomposition
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@TraceRecomposition
@Composable
fun SleepScreen(
    viewModel: SleepViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStatistics: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val onStart = remember(viewModel) { { viewModel.sendIntent(SleepIntent.StartSleep) } }
    val onStop = remember(viewModel) { { viewModel.sendIntent(SleepIntent.StopSleep) } }
    val onSelectType = remember(viewModel) {
        { type: SleepType -> viewModel.sendIntent(SleepIntent.SelectSleepType(type)) }
    }
    val onShowLog = remember(viewModel) { { viewModel.sendIntent(SleepIntent.ShowLogDialog) } }
    val onDismissLog = remember(viewModel) { { viewModel.sendIntent(SleepIntent.DismissLogDialog) } }
    val onLogConfirm = remember(viewModel) {
        { start: Long, end: Long -> viewModel.sendIntent(SleepIntent.LogPastSleep(start, end)) }
    }
    val onOpenStats = remember(viewModel) { { viewModel.sendIntent(SleepIntent.OpenStatistics) } }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                SleepEffect.NavigateToStatistics -> onNavigateToStatistics()
                is SleepEffect.ShowError ->
                    snackbarHostState.showSnackbar(context.getString(effect.message))
            }
        }
    }

    if (state.showLogDialog) {
        LogSleepDialog(
            onConfirm = onLogConfirm,
            onDismiss = onDismissLog,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sleep_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenStats) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.sleep_stats_open),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (!state.isRunning) {
                FloatingActionButton(onClick = onShowLog) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.sleep_log_past))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ThenaTheme.spacing.lg, vertical = ThenaTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.md),
        ) {
            SleepTimerCard(
                isRunning = state.isRunning,
                elapsedSeconds = state.elapsedSeconds,
                activeSleepType = state.activeSleepType,
                onStart = onStart,
                onStop = onStop,
                onSelectType = onSelectType,
            )

            val stats = state.todayStats
            if (stats != null) {
                SleepGoalCard(stats = stats)
            }

            if (state.sessions.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.sleep_sessions_today),
                    style = ThenaTheme.typography.titleMedium,
                )
                state.sessions.forEach { session ->
                    SleepSessionItem(session = session)
                    Spacer(modifier = Modifier.height(ThenaTheme.spacing.xs))
                }
            }
        }
    }
}
