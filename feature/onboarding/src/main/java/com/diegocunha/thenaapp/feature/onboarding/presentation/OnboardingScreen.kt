package com.diegocunha.thenaapp.feature.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diegocunha.thenaapp.coreui.theme.ThenaExtendedColors
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.onboarding.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToLogin: () -> Unit,
) {
    val onSkip = remember(viewModel) { { viewModel.sendIntent(OnboardingIntent.Skip) } }
    val onDone = remember(viewModel) { { viewModel.sendIntent(OnboardingIntent.Done) } }
    val onPageChanged = remember(viewModel) { { page: Int -> viewModel.sendIntent(OnboardingIntent.PageChanged(page)) } }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                OnboardingEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    OnboardingScreenContent(
        onSkip = onSkip,
        onDone = onDone,
        onPageChanged = onPageChanged,
    )
}

@Composable
private fun OnboardingScreenContent(
    onSkip: () -> Unit,
    onDone: () -> Unit,
    onPageChanged: (Int) -> Unit,
) {
    val colors = ThenaTheme.colors
    val extendedColors = ThenaTheme.extendedColors
    val spacing = ThenaTheme.spacing

    val slides = slideContent(colors, extendedColors)
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val isLast by remember {
        derivedStateOf { pagerState.currentPage == slides.size - 1 }
    }

    LaunchedEffect(currentPage) {
        onPageChanged(currentPage)
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.surface),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = spacing.sm, top = spacing.sm),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (!isLast) {
                    TextButton(onClick = onSkip) {
                        Text(
                            text = "Skip",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                OnboardingSlideContent(slide = slides[page])
            }

            Column(
                modifier = Modifier.padding(
                    horizontal = spacing.xl,
                    vertical = spacing.xl,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    slides.forEachIndexed { index, _ ->
                        val isActive = index == currentPage
                        Box(
                            modifier = Modifier
                                .height(spacing.sm)
                                .width(if (isActive) spacing.lg else spacing.sm)
                                .clip(RoundedCornerShape(spacing.xs))
                                .background(if (isActive) slides[currentPage].accent else colors.outlineVariant),
                        )
                    }
                }

                if (isLast) {
                    Button(
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Get started 🌸")
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (currentPage > 0) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(currentPage - 1)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Back")
                            }
                        }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(currentPage + 1)
                                }
                            },
                            modifier = Modifier.weight(if (currentPage > 0) 2f else 1f),
                            colors = ButtonDefaults.buttonColors(containerColor = slides[currentPage].accent),
                        ) {
                            Text("Next →", color = Color.White)
                        }
                    }
                }
            }
        }
    }


}

@Composable
private fun OnboardingSlideContent(slide: OnboardingSlide) {
    val colors = ThenaTheme.colors
    val spacing = ThenaTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.xl),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(spacing.xl))
                .background(slide.color)
                .padding(horizontal = spacing.xl, vertical = spacing.xxxl),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = slide.emoji, fontSize = 52.sp)
                }

                Text(
                    text = slide.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onBackground,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = slide.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            slide.features.forEach { feature ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.xl)
                            .clip(RoundedCornerShape(spacing.sm))
                            .background(colors.surfaceContainerHigh),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(spacing.sm)
                                .clip(CircleShape)
                                .background(slide.accent),
                        )
                    }
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun slideContent(
    colors: ColorScheme,
    extendedColors: ThenaExtendedColors
): List<OnboardingSlide> {
    val welcomeTitle = stringResource(R.string.onboarding_welcome_title)
    val welcomeSubtitle = stringResource(R.string.onboarding_welcome_subtitle)

    val sleepTitle = stringResource(R.string.onboarding_sleep_title)
    val sleepSubtitle = stringResource(R.string.onboarding_sleep_subtitle)
    val sleepF1 = stringResource(R.string.onboarding_sleep_feature_1)
    val sleepF2 = stringResource(R.string.onboarding_sleep_feature_2)
    val sleepF3 = stringResource(R.string.onboarding_sleep_feature_3)

    val feedTitle = stringResource(R.string.onboarding_feed_title)
    val feedSubtitle = stringResource(R.string.onboarding_feed_subtitle)
    val feedF1 = stringResource(R.string.onboarding_feed_feature_1)
    val feedF2 = stringResource(R.string.onboarding_feed_feature_2)
    val feedF3 = stringResource(R.string.onboarding_feed_feature_3)

    val vaccineTitle = stringResource(R.string.onboarding_vaccine_title)
    val vaccineSubtitle = stringResource(R.string.onboarding_vaccine_subtitle)
    val vaccineF1 = stringResource(R.string.onboarding_vaccine_feature_1)
    val vaccineF2 = stringResource(R.string.onboarding_vaccine_feature_2)
    val vaccineF3 = stringResource(R.string.onboarding_vaccine_feature_3)

    val insightTitle = stringResource(R.string.onboarding_insight_title)
    val insightSubtitle = stringResource(R.string.onboarding_insight_subtitle)
    val insightF1 = stringResource(R.string.onboarding_insight_feature_1)
    val insightF2 = stringResource(R.string.onboarding_insight_feature_2)
    val insightF3 = stringResource(R.string.onboarding_insight_feature_3)

    return remember(colors, extendedColors) {
        listOf(
            OnboardingSlide(
                emoji = "🌸",
                color = colors.primaryContainer,
                accent = colors.primary,
                title = welcomeTitle,
                subtitle = welcomeSubtitle,
                features = emptyList(),
            ),
            OnboardingSlide(
                emoji = "🌙",
                color = extendedColors.sleepFill,
                accent = colors.primary,
                title = sleepTitle,
                subtitle = sleepSubtitle,
                features = listOf(sleepF1, sleepF2, sleepF3),
            ),
            OnboardingSlide(
                emoji = "🍼",
                color = extendedColors.feedFill,
                accent = colors.secondary,
                title = feedTitle,
                subtitle = feedSubtitle,
                features = listOf(feedF1, feedF2, feedF3),
            ),
            OnboardingSlide(
                emoji = "💉",
                color = extendedColors.vaccineFill,
                accent = Color(0xFF4CAF50),
                title = vaccineTitle,
                subtitle = vaccineSubtitle,
                features = listOf(vaccineF1, vaccineF2, vaccineF3),
            ),
            OnboardingSlide(
                emoji = "📊",
                color = extendedColors.summaryFill,
                accent = colors.tertiary,
                title = insightTitle,
                subtitle = insightSubtitle,
                features = listOf(insightF1, insightF2, insightF3),
            ),
        )
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreenContent(
        onSkip = {},
        onDone = {},
        onPageChanged = {}
    )
}
