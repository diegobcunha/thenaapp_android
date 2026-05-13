 package com.diegocunha.thenaapp.coreui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

private val rowModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 8.dp)

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PeriodFilterSelectedTodayPreview() {
    ThenaTheme {
        Row(modifier = rowModifier) {
            Period.entries.forEach { period ->
                PeriodFilerComponent(
                    modifier = Modifier.weight(1f),
                    period = period,
                    selected = Period.TODAY,
                    onSelected = {}
                )
            }
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PeriodFilterSelectedTodayDarkPreview() {
    ThenaTheme(darkTheme = true) {
        Row(modifier = rowModifier) {
            Period.entries.forEach { period ->
                PeriodFilerComponent(
                    modifier = Modifier.weight(1f),
                    period = period,
                    selected = Period.TODAY,
                    onSelected = {}
                )
            }
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PeriodFilterSelectedWeekPreview() {
    ThenaTheme {
        Row(modifier = rowModifier) {
            Period.entries.forEach { period ->
                PeriodFilerComponent(
                    modifier = Modifier.weight(1f),
                    period = period,
                    selected = Period.WEEK,
                    onSelected = {}
                )
            }
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PeriodFilterSelectedWeekDarkPreview() {
    ThenaTheme(darkTheme = true) {
        Row(modifier = rowModifier) {
            Period.entries.forEach { period ->
                PeriodFilerComponent(
                    modifier = Modifier.weight(1f),
                    period = period,
                    selected = Period.WEEK,
                    onSelected = {}
                )
            }
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PeriodFilterSelectedMonthPreview() {
    ThenaTheme {
        Row(modifier = rowModifier) {
            Period.entries.forEach { period ->
                PeriodFilerComponent(
                    modifier = Modifier.weight(1f),
                    period = period,
                    selected = Period.MONTH,
                    onSelected = {}
                )
            }
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PeriodFilterSelectedMonthDarkPreview() {
    ThenaTheme(darkTheme = true) {
        Row(modifier = rowModifier) {
            Period.entries.forEach { period ->
                PeriodFilerComponent(
                    modifier = Modifier.weight(1f),
                    period = period,
                    selected = Period.MONTH,
                    onSelected = {}
                )
            }
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PeriodFilterSelectedCustomPreview() {
    ThenaTheme {
        Row(modifier = rowModifier) {
            Period.entries.forEach { period ->
                PeriodFilerComponent(
                    modifier = Modifier.weight(1f),
                    period = period,
                    selected = Period.CUSTOM,
                    onSelected = {}
                )
            }
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PeriodFilterSelectedCustomDarkPreview() {
    ThenaTheme(darkTheme = true) {
        Row(modifier = rowModifier) {
            Period.entries.forEach { period ->
                PeriodFilerComponent(
                    modifier = Modifier.weight(1f),
                    period = period,
                    selected = Period.CUSTOM,
                    onSelected = {}
                )
            }
        }
    }
}