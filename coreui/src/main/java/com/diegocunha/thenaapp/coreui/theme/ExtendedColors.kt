package com.diegocunha.thenaapp.coreui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ThenaExtendedColors(
    val sleepFill: Color,
    val feedFill: Color,
    val vaccineFill: Color,
    val summaryFill: Color,
)

internal val thenaLightExtendedColors = ThenaExtendedColors(
    sleepFill = Color(0xFFDDD0F7),
    feedFill = Color(0xFFFFD8EE),
    vaccineFill = Color(0xFFC8F0E8),
    summaryFill = Color(0xFFFFE9C8),
)

internal val thenaDarkExtendedColors = ThenaExtendedColors(
    sleepFill = Color(0xFF3D2F6A),
    feedFill = Color(0xFF5C2040),
    vaccineFill = Color(0xFF1A4A40),
    summaryFill = Color(0xFF4A3000),
)

val LocalThenaExtendedColors = staticCompositionLocalOf { thenaLightExtendedColors }