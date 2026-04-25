package com.diegocunha.thenaapp.feature.home.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.diegocunha.thenaapp.coreui.component.CardButtonInformation
import com.diegocunha.thenaapp.coreui.component.LoadingComponent
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.home.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val notDevelopedYetMessage = stringResource(R.string.home_not_developed_yet)
    val onEditClick = remember(viewModel) { { viewModel.sendIntent(HomeIntent.EditBabyInfo) } }
    val onUserProfileClick =
        remember(viewModel) { { viewModel.sendIntent(HomeIntent.UserProfile) } }
    val onSleepClick = remember { { viewModel.sendIntent(HomeIntent.SleepInfo) } }
    val onFeedingClick = remember { { viewModel.sendIntent(HomeIntent.FeedInfo) } }
    val onVaccineClick = remember { { viewModel.sendIntent(HomeIntent.VaccineInfo) } }
    val onSummaryClick = remember { { viewModel.sendIntent(HomeIntent.SummaryInfo) } }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is HomeEffect.NotDevelopedYet -> snackbarHostState.showSnackbar(
                    notDevelopedYetMessage
                )
            }
        }
    }

    HomeScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEditClick = onEditClick,
        onUserProfileClick = onUserProfileClick,
        onSleepClick = onSleepClick,
        onFeedingClick = onFeedingClick,
        onVaccineClick = onVaccineClick,
        onSummaryClick = onSummaryClick,
    )
}

@Composable
private fun HomeScreenContent(
    state: HomeState,
    snackbarHostState: SnackbarHostState,
    onEditClick: () -> Unit,
    onUserProfileClick: () -> Unit,
    onSleepClick: () -> Unit,
    onFeedingClick: () -> Unit,
    onVaccineClick: () -> Unit,
    onSummaryClick: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                LoadingComponent()
            } else {
                Home(
                    state = state,
                    onEditClick = onEditClick,
                    onUserProfileClick = onUserProfileClick,
                    onSleepClick = onSleepClick,
                    onFeedingClick = onFeedingClick,
                    onVaccineClick = onVaccineClick,
                    onSummaryClick = onSummaryClick,
                )
            }
        }
    }
}

@Composable
private fun Home(
    state: HomeState,
    onEditClick: () -> Unit,
    onUserProfileClick: () -> Unit,
    onSleepClick: () -> Unit,
    onFeedingClick: () -> Unit,
    onVaccineClick: () -> Unit,
    onSummaryClick: () -> Unit,
) {
    val colors = ThenaTheme.colors
    val babyAgeText = state.babyAge?.toDisplayString().orEmpty()

    val headerBrush = remember(colors.primaryContainer, colors.secondaryContainer) {
        Brush.linearGradient(listOf(colors.primaryContainer, colors.secondaryContainer))
    }

    val quickLogTitleStyle: TextStyle = ThenaTheme.typography.titleMedium
    var quickLogTitleFontSize by remember { mutableStateOf(quickLogTitleStyle.fontSize) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBrush),
        ) {
            Column(
                modifier = Modifier
                    .padding(ThenaTheme.spacing.md)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.home_welcome, state.userName),
                        style = ThenaTheme.typography.displaySmall,
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colors.primaryContainer)
                            .border(
                                width = 2.dp,
                                brush = SolidColor(colors.primary),
                                shape = CircleShape,
                            )
                            .clickable(onClick = onUserProfileClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            state.userName.first().toString(),
                            style = ThenaTheme.typography.titleLarge
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .padding(top = ThenaTheme.spacing.sm)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ThenaTheme.colors.surface)
                            .padding(ThenaTheme.spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer)
                                .border(
                                    width = 2.dp,
                                    brush = SolidColor(colors.primary),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (state.babyPhotoUrl != null) {
                                AsyncImage(
                                    model = state.babyPhotoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Text(text = "🍼", fontSize = 24.sp)
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = state.babyName,
                                style = ThenaTheme.typography.bodyMedium,
                            )
                            Text(
                                babyAgeText,
                                style = ThenaTheme.typography.bodySmall,
                            )
                            Text(
                                stringResource(
                                    R.string.home_baby_info,
                                    state.babyInfo?.weight.orEmpty(),
                                    state.babyInfo?.height.orEmpty()
                                ),
                                style = ThenaTheme.typography.bodySmall,
                            )
                        }

                        Button(
                            modifier = Modifier
                                .wrapContentWidth(),
                            onClick = onEditClick
                        ) { Text(stringResource(R.string.home_edit_info)) }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThenaTheme.spacing.md)
        ) {
            Text(
                stringResource(R.string.home_quick_log_title),
                style = ThenaTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ThenaTheme.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {

                CardButtonInformation(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onSleepClick),
                    color = ThenaTheme.extendedColors.sleepFill,
                    headerInformation = { Text("🌙") },
                    titleInformation = {
                        Text(
                            stringResource(R.string.home_quick_log_sleep),
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                            style = ThenaTheme.typography.titleMedium.copy(fontSize = quickLogTitleFontSize),
                            textAlign = TextAlign.Center,
                            onTextLayout = { result ->
                                if (result.hasVisualOverflow && quickLogTitleFontSize > 8.sp) {
                                    quickLogTitleFontSize *= 0.9f
                                }
                            }
                        )
                    }
                )

                CardButtonInformation(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onFeedingClick),
                    color = ThenaTheme.extendedColors.feedFill,
                    headerInformation = { Text("🍼") },
                    titleInformation = {
                        Text(
                            stringResource(R.string.home_quick_log_feed),
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                            style = ThenaTheme.typography.titleMedium.copy(fontSize = quickLogTitleFontSize),
                            textAlign = TextAlign.Center,
                            onTextLayout = { result ->
                                if (result.hasVisualOverflow && quickLogTitleFontSize > 8.sp) {
                                    quickLogTitleFontSize *= 0.9f
                                }
                            }
                        )
                    }
                )

                CardButtonInformation(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onVaccineClick),
                    color = ThenaTheme.extendedColors.vaccineFill,
                    headerInformation = { Text("💉") },
                    titleInformation = {
                        Text(
                            stringResource(R.string.home_quick_log_vaccine),
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                            style = ThenaTheme.typography.titleMedium.copy(fontSize = quickLogTitleFontSize),
                            textAlign = TextAlign.Center,
                            onTextLayout = { result ->
                                if (result.hasVisualOverflow && quickLogTitleFontSize > 8.sp) {
                                    quickLogTitleFontSize *= 0.9f
                                }
                            }
                        )
                    }
                )

                CardButtonInformation(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onVaccineClick),
                    color = ThenaTheme.extendedColors.summaryFill,
                    headerInformation = { Text("📊") },
                    titleInformation = {
                        Text(
                            stringResource(R.string.home_quick_log_summary),
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                            style = ThenaTheme.typography.titleMedium.copy(fontSize = quickLogTitleFontSize),
                            textAlign = TextAlign.Center,
                            onTextLayout = { result ->
                                if (result.hasVisualOverflow && quickLogTitleFontSize > 8.sp) {
                                    quickLogTitleFontSize *= 0.9f
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}


@Composable
private fun BabyAge.toDisplayString(): String {
    val yearsText = pluralStringResource(R.plurals.home_baby_age_years, years, years)
    val monthsText =
        pluralStringResource(R.plurals.home_baby_age_months, remainderMonths, remainderMonths)
    val totalMonthsText =
        pluralStringResource(R.plurals.home_baby_age_months, totalMonths, totalMonths)
    return when {
        totalMonths < 12 -> totalMonthsText
        remainderMonths == 0 -> yearsText
        else -> "$yearsText $monthsText"
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreviewDark() {
    ThenaTheme {
        HomeScreenContent(
            state = HomeState(
                isLoading = false,
                "Diego!",
                babyName = "Theo",
                babyAge = BabyAge(totalMonths = 14, years = 1, remainderMonths = 2),
                babyInfo = BabyInfo("53", "4.220"),
                babyPhotoUrl = null
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEditClick = {},
            onUserProfileClick = {},
            onSleepClick = {},
            onFeedingClick = {},
            onSummaryClick = {},
            onVaccineClick = {},
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreviewLight() {
    ThenaTheme {
        HomeScreenContent(
            state = HomeState(
                isLoading = false,
                "Diego!",
                babyName = "Theo",
                babyAge = BabyAge(totalMonths = 14, years = 1, remainderMonths = 2),
                babyInfo = BabyInfo("53", "4.220"),
                babyPhotoUrl = null
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEditClick = {},
            onUserProfileClick = {},
            onSleepClick = {},
            onFeedingClick = {},
            onSummaryClick = {},
            onVaccineClick = {},
        )
    }
}
