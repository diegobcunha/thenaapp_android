package com.diegocunha.thenaapp.feature.feeding.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.feeding.R
import com.diegocunha.thenaapp.feature.feeding.domain.model.FeedingType

@Composable
fun FeedingTypePicker(
    selected: FeedingType?,
    onSelect: (FeedingType) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
    ) {
        FeedingTypeChip(
            modifier = Modifier.weight(1f),
            emoji = "🤱",
            label = stringResource(R.string.feeding_type_breastfeed),
            isSelected = selected == null || selected == FeedingType.BREAST,
            onClick = { onSelect(FeedingType.BREAST) },
        )
        FeedingTypeChip(
            modifier = Modifier.weight(1f),
            emoji = "🍼",
            label = stringResource(R.string.feeding_type_bottle),
            isSelected = selected == FeedingType.BOTTLE,
            onClick = { onSelect(FeedingType.BOTTLE) },
        )
    }
}
