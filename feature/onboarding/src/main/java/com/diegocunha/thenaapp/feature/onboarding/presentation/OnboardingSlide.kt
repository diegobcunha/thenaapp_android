package com.diegocunha.thenaapp.feature.onboarding.presentation

import androidx.compose.ui.graphics.Color

data class OnboardingSlide(
    val emoji: String,
    val color: Color,
    val accent: Color,
    val title: String,
    val subtitle: String,
    val features: List<String>,
)
