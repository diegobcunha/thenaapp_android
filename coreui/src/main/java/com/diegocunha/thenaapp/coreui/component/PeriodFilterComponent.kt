package com.diegocunha.thenaapp.coreui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.diegocunha.thenaapp.coreui.R
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

enum class Period(
    @StringRes val textRes: Int,
) {
    TODAY(R.string.coreui_period_today),
    WEEK(R.string.coreui_period_week),
    MONTH(R.string.coreui_period_month),
    CUSTOM(R.string.coreui_period_custom)
}

@Composable
fun PeriodFilerComponent(
    period: Period,
    selected: Period,
    onSelected: (Period) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        modifier = modifier,
        selected = selected == period,
        onClick = { onSelected(period) },
        label = {
            Text(
                text = stringResource(period.textRes),
                style = ThenaTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        },
    )
}

@Preview
@Composable
private fun PeriodFilterPreview() {
    ThenaTheme {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            PeriodFilerComponent(
                modifier = Modifier.weight(1f),
                period = Period.MONTH,
                selected = Period.WEEK,
                onSelected = {}
            )
        }
    }
}
