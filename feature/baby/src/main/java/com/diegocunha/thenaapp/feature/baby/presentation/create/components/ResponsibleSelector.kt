package com.diegocunha.thenaapp.feature.baby.presentation.create.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diegocunha.thenaapp.coreui.component.OptionalButton
import com.diegocunha.thenaapp.feature.baby.R
import com.diegocunha.thenaapp.feature.baby.domain.model.ResponsibleType

@Composable
fun ResponsibleSelector(
    selected: ResponsibleType?,
    onSelect: (ResponsibleType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OptionalButton(
            label = stringResource(R.string.create_baby_responsible_mother),
            emoji = "👧",
            selected = selected == ResponsibleType.MOTHER,
            onClick = { onSelect(ResponsibleType.MOTHER) },
            modifier = Modifier.weight(1f),
        )
        OptionalButton(
            label = stringResource(R.string.create_baby_responsible_father),
            emoji = "👦",
            selected = selected == ResponsibleType.FATHER,
            onClick = { onSelect(ResponsibleType.FATHER) },
            modifier = Modifier.weight(1f),
        )
        OptionalButton(
            label = stringResource(R.string.create_baby_gender_other),
            emoji = "🌈",
            selected = selected == ResponsibleType.OTHER,
            onClick = { onSelect(ResponsibleType.OTHER) },
            modifier = Modifier.weight(1f),
        )
    }
}
