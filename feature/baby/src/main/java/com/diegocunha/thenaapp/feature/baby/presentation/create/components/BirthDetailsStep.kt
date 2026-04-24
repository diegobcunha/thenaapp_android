package com.diegocunha.thenaapp.feature.baby.presentation.create.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.baby.R
import com.diegocunha.thenaapp.feature.baby.presentation.create.util.DateMaskVisualTransformation
import java.util.Locale

@Composable
fun BirthDetailsStep(
    birthDate: String,
    @StringRes birthDateError: Int?,
    birthWeight: String,
    @StringRes birthWeightError: Int?,
    birthHeight: String,
    @StringRes birthHeightError: Int?,
    onBirthDateChange: (String) -> Unit,
    onBirthWeightChange: (String) -> Unit,
    onBirthHeightChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ThenaTheme.spacing
    val colors = MaterialTheme.colorScheme
    val isYMD = !Locale.getDefault().language.startsWith("pt")
    val dateHint = stringResource(if (isYMD) R.string.create_baby_dob_hint_ymd else R.string.create_baby_dob_hint_dmy)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.lg, vertical = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        OutlinedTextField(
            value = birthDate,
            onValueChange = { input ->
                val digits = input.filter { it.isDigit() }.take(8)
                onBirthDateChange(digits)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.create_baby_dob_label)) },
            placeholder = { Text(dateHint, color = colors.onSurfaceVariant) },
            visualTransformation = DateMaskVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = birthDateError != null,
            supportingText = birthDateError?.let { id -> { Text(stringResource(id)) } },
            singleLine = true,
        )

        OutlinedTextField(
            value = birthWeight,
            onValueChange = onBirthWeightChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.create_baby_weight_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = birthWeightError != null,
            supportingText = birthWeightError?.let { id -> { Text(stringResource(id)) } },
            singleLine = true,
        )

        OutlinedTextField(
            value = birthHeight,
            onValueChange = onBirthHeightChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.create_baby_height_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = birthHeightError != null,
            supportingText = birthHeightError?.let { id -> { Text(stringResource(id)) } },
            singleLine = true,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.tertiaryContainer),
        ) {
            Row(
                modifier = Modifier.padding(spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Text("💡", fontSize = 20.sp)
                Text(
                    text = stringResource(R.string.create_baby_info_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.create_baby_back))
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(2f),
            ) {
                Text(stringResource(R.string.create_baby_next))
            }
        }
    }
}
