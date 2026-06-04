package com.diegocunha.thenaapp.feature.vaccine.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diegocunha.thenaapp.coreui.component.LoadingComponent
import com.diegocunha.thenaapp.coreui.icon.ThenaIcon
import com.diegocunha.thenaapp.coreui.icon.ThenaIcons
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.vaccine.R
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineRecord
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineScheduleItem
import com.diegocunha.thenaapp.feature.vaccine.domain.model.VaccineScheduleStatus
import com.skydoves.compose.stability.runtime.TraceRecomposition
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@TraceRecomposition
@Composable
fun VaccineScreen(
    viewModel: VaccineViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRegister: (babyId: String, pniTemplateId: String?, vaccineName: String?) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is VaccineEffect.NavigateToRegister ->
                    onNavigateToRegister(effect.babyId, effect.pniTemplateId, effect.vaccineName)
                is VaccineEffect.ShowError ->
                    snackbarHostState.showSnackbar(context.getString(effect.message))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vaccine_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.sendIntent(VaccineIntent.RegisterVaccine) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.vaccine_register_fab))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading) {
            LoadingComponent()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            VaccineProgressCard(
                completedCount = state.completedCount,
                totalCount = state.totalCount,
                modifier = Modifier.padding(ThenaTheme.spacing.md),
            )

            if (state.hasUrgent) {
                UrgentAlertCard(modifier = Modifier.padding(horizontal = ThenaTheme.spacing.md))
                Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))
            }

            TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                VaccineTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.sendIntent(VaccineIntent.SelectTab(tab)) },
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(stringResource(tab.labelRes))
                                val count = if (tab == VaccineTab.UPCOMING)
                                    state.scheduleItems.count { it.isUpcoming }
                                else state.completedCount
                                if (count > 0) {
                                    Badge { Text(count.toString()) }
                                }
                            }
                        },
                    )
                }
            }

            when (state.selectedTab) {
                VaccineTab.UPCOMING -> UpcomingList(
                    items = state.scheduleItems.filter { it.isUpcoming },
                    onRegisterFromSchedule = { pniTemplateId ->
                        viewModel.sendIntent(VaccineIntent.RegisterFromSchedule(pniTemplateId))
                    },
                )
                VaccineTab.COMPLETED -> CompletedList(
                    records = state.records,
                    onDelete = { recordId ->
                        viewModel.sendIntent(VaccineIntent.DeleteRecord(recordId))
                    },
                )
            }
        }
    }
}

@Composable
private fun VaccineProgressCard(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(ThenaTheme.spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ThenaIcon(
                        icon = ThenaIcons.Vaccine,
                        modifier = Modifier.size(ThenaTheme.spacing.lg),
                        tint = ThenaTheme.extendedColors.vaccineFill,
                    )
                    Text(
                        stringResource(R.string.vaccine_progress_title),
                        style = ThenaTheme.typography.titleMedium,
                    )
                }
                Text(
                    "$completedCount / $totalCount",
                    style = ThenaTheme.typography.titleSmall,
                    color = ThenaTheme.colors.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(ThenaTheme.spacing.sm))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = ThenaTheme.extendedColors.vaccineFill,
                trackColor = ThenaTheme.colors.surfaceVariant,
            )
        }
    }
}

@Composable
private fun UrgentAlertCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(ThenaTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                stringResource(R.string.vaccine_urgent_alert),
                style = ThenaTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun UpcomingList(
    items: List<VaccineScheduleItem>,
    onRegisterFromSchedule: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(ThenaTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
    ) {
        items(items, key = { it.pniTemplateId }) { item ->
            VaccineScheduleListItem(item = item, onRegister = { onRegisterFromSchedule(item.pniTemplateId) })
        }
    }
}

@Composable
private fun CompletedList(
    records: List<VaccineRecord>,
    onDelete: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(ThenaTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
    ) {
        items(records, key = { it.id }) { record ->
            VaccineRecordListItem(record = record, onDelete = { onDelete(record.id) })
        }
    }
}

@Composable
private fun VaccineScheduleListItem(
    item: VaccineScheduleItem,
    onRegister: () -> Unit,
) {
    val statusColor = when (item.status) {
        VaccineScheduleStatus.OVERDUE -> MaterialTheme.colorScheme.error
        VaccineScheduleStatus.DUE -> MaterialTheme.colorScheme.primary
        else -> ThenaTheme.colors.onSurfaceVariant
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThenaTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.vaccineName, style = ThenaTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.vaccine_dose_number, item.doseNumber),
                    style = ThenaTheme.typography.bodySmall,
                    color = ThenaTheme.colors.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.vaccine_recommended_age, item.recommendedAgeMonths),
                    style = ThenaTheme.typography.bodySmall,
                    color = statusColor,
                )
            }
            TextButton(onClick = onRegister) {
                Text(stringResource(R.string.vaccine_register_action))
            }
        }
    }
}

@Composable
private fun VaccineRecordListItem(
    record: VaccineRecord,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThenaTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ThenaTheme.extendedColors.vaccineFill,
                    modifier = Modifier.size(ThenaTheme.spacing.lg),
                )
                Column {
                    Text(record.vaccineName, style = ThenaTheme.typography.titleSmall)
                    Text(
                        record.administeredDate,
                        style = ThenaTheme.typography.bodySmall,
                        color = ThenaTheme.colors.onSurfaceVariant,
                    )
                }
            }
            TextButton(onClick = onDelete) {
                Text(
                    stringResource(R.string.vaccine_delete_action),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private val VaccineTab.labelRes: Int
    get() = when (this) {
        VaccineTab.UPCOMING -> R.string.vaccine_tab_upcoming
        VaccineTab.COMPLETED -> R.string.vaccine_tab_completed
    }