package com.diegocunha.thenaapp.feature.baby.presentation.create.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diegocunha.thenaapp.coreui.component.OptionalButton
import com.diegocunha.thenaapp.coreui.icon.ThenaIcons
import com.diegocunha.thenaapp.feature.baby.R
import com.diegocunha.thenaapp.feature.baby.domain.model.BabyGender

@Composable
fun GenderSelector(
    selected: BabyGender?,
    onSelect: (BabyGender) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OptionalButton(
            label = stringResource(R.string.create_baby_gender_girl),
            icon = ThenaIcons.Female,
            selected = selected == BabyGender.GIRL,
            onClick = { onSelect(BabyGender.GIRL) },
            modifier = Modifier.weight(1f),
        )
        OptionalButton(
            label = stringResource(R.string.create_baby_gender_boy),
            icon = ThenaIcons.Male,
            selected = selected == BabyGender.BOY,
            onClick = { onSelect(BabyGender.BOY) },
            modifier = Modifier.weight(1f),
        )
        OptionalButton(
            label = stringResource(R.string.create_baby_gender_other),
            icon = ThenaIcons.People,
            selected = selected == BabyGender.OTHER,
            onClick = { onSelect(BabyGender.OTHER) },
            modifier = Modifier.weight(1f),
        )
    }
}
