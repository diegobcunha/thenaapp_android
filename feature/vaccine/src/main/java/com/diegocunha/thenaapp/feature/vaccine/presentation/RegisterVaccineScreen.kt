package com.diegocunha.thenaapp.feature.vaccine.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diegocunha.thenaapp.coreui.component.DateMaskVisualTransformation
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.vaccine.R
import com.diegocunha.thenaapp.feature.vaccine.domain.model.DoseType
import com.diegocunha.thenaapp.feature.vaccine.domain.model.InjectionSite
import com.diegocunha.thenaapp.feature.vaccine.domain.model.ReactionSeverity
import com.skydoves.compose.stability.runtime.TraceRecomposition
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@TraceRecomposition
@Composable
fun RegisterVaccineScreen(
    viewModel: RegisterVaccineViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                RegisterVaccineEffect.NavigateBack -> onNavigateBack()
                is RegisterVaccineEffect.ShowError ->
                    snackbarHostState.showSnackbar(context.getString(effect.message))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vaccine_register_title)) },
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
                .padding(ThenaTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.md),
        ) {
            OutlinedTextField(
                value = state.vaccineName,
                onValueChange = { viewModel.sendIntent(RegisterVaccineIntent.UpdateVaccineName(it)) },
                label = { Text(stringResource(R.string.vaccine_field_name)) },
                modifier = Modifier.fillMaxWidth(),
                isError = state.vaccineNameError,
                supportingText = if (state.vaccineNameError) {
                    { Text(stringResource(R.string.vaccine_field_name_error)) }
                } else null,
                enabled = !state.vaccineNameLocked,
                singleLine = true,
            )

            OutlinedTextField(
                value = state.administeredDate,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(8)
                    viewModel.sendIntent(RegisterVaccineIntent.UpdateDate(digits))
                },
                label = { Text(stringResource(R.string.vaccine_field_date)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = DateMaskVisualTransformation(),
                isError = state.dateError,
                supportingText = if (state.dateError) {
                    { Text(stringResource(R.string.vaccine_field_date_error)) }
                } else null,
                singleLine = true,
            )

            EnumDropdown(
                label = stringResource(R.string.vaccine_field_dose_type),
                selected = state.doseType,
                options = DoseType.entries,
                displayName = { it.name.replace('_', ' ') },
                onSelect = { viewModel.sendIntent(RegisterVaccineIntent.UpdateDoseType(it)) },
            )

            EnumDropdown(
                label = stringResource(R.string.vaccine_field_injection_site),
                selected = state.injectionSite,
                options = listOf(null) + InjectionSite.entries,
                displayName = { it?.name?.replace('_', ' ') ?: stringResource(R.string.vaccine_field_not_applicable) },
                onSelect = { viewModel.sendIntent(RegisterVaccineIntent.UpdateInjectionSite(it)) },
            )

            OutlinedTextField(
                value = state.batchNumber,
                onValueChange = { viewModel.sendIntent(RegisterVaccineIntent.UpdateBatchNumber(it)) },
                label = { Text(stringResource(R.string.vaccine_field_batch)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.healthcareProvider,
                onValueChange = { viewModel.sendIntent(RegisterVaccineIntent.UpdateHealthcareProvider(it)) },
                label = { Text(stringResource(R.string.vaccine_field_provider)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.vaccine_field_reaction),
                    style = ThenaTheme.typography.bodyMedium,
                )
                Switch(
                    checked = state.hasReaction,
                    onCheckedChange = { viewModel.sendIntent(RegisterVaccineIntent.ToggleReaction(it)) },
                )
            }

            if (state.hasReaction) {
                EnumDropdown(
                    label = stringResource(R.string.vaccine_field_reaction_severity),
                    selected = state.reactionSeverity,
                    options = ReactionSeverity.entries,
                    displayName = { it.name },
                    onSelect = { viewModel.sendIntent(RegisterVaccineIntent.UpdateReactionSeverity(it)) },
                )

                OutlinedTextField(
                    value = state.reactionNotes,
                    onValueChange = { viewModel.sendIntent(RegisterVaccineIntent.UpdateReactionNotes(it)) },
                    label = { Text(stringResource(R.string.vaccine_field_reaction_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                )
            }

            Button(
                onClick = { viewModel.sendIntent(RegisterVaccineIntent.Submit) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting,
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = ThenaTheme.spacing.sm))
                }
                Text(stringResource(R.string.vaccine_register_submit))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    selected: T,
    options: List<T>,
    displayName: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = displayName(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(displayName(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}