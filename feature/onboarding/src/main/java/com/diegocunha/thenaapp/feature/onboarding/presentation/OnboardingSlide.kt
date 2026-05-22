package com.diegocunha.thenaapp.feature.onboarding.presentation

import androidx.compose.ui.graphics.Color
import com.diegocunha.thenaapp.coreui.icon.ThenaIcon
import kotlinx.collections.immutable.ImmutableList

data class OnboardingSlide(
    val icon: ThenaIcon,
    val color: Color,
    val accent: Color,
    val title: String,
    val subtitle: String,
    val features: ImmutableList<String>,
)