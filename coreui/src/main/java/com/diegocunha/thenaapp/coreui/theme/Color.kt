package com.diegocunha.thenaapp.coreui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val LightPrimary = Color(0xFF7C5CBF)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFEADDFF)
private val LightOnPrimaryContainer = Color(0xFF21005D)
private val LightSecondary = Color(0xFFC774A0)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFFFD8EE)
private val LightOnSecondaryContainer = Color(0xFF3E0029)
private val LightTertiary = Color(0xFFF2A65A)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightTertiaryContainer = Color(0xFFFFE0C2)
private val LightOnTertiaryContainer = Color(0xFF2D1600)
private val LightError = Color(0xFFBA1A1A)
private val LightOnError = Color(0xFFFFFFFF)
private val LightErrorContainer = Color(0xFFFFDAD6)
private val LightOnErrorContainer = Color(0xFF410002)
private val LightBackground = Color(0xFFFFFBFE)
private val LightOnBackground = Color(0xFF1C1B1F)
private val LightSurface = Color(0xFFFFFBFE)
private val LightOnSurface = Color(0xFF1C1B1F)
private val LightSurfaceVariant = Color(0xFFE7E0EC)
private val LightOnSurfaceVariant = Color(0xFF49454F)
private val LightOutline = Color(0xFF79747E)
private val LightOutlineVariant = Color(0xFFCAC4D0)
private val LightInverseSurface = Color(0xFF313033)
private val LightInverseOnSurface = Color(0xFFF4EFF4)
private val LightInversePrimary = Color(0xFFD0BCFF)
private val LightSurfaceTint = Color(0xFF7C5CBF)
private val LightSurfaceContainer = Color(0xFFF3EDF7)
private val LightSurfaceContainerHigh = Color(0xFFECE6F0)
private val LightSurfaceContainerHighest = Color(0xFFE6E0E9)
private val LightSurfaceContainerLow = Color(0xFFF7F2FA)
private val LightSurfaceContainerLowest = Color(0xFFFFFFFF)

/**
 * Dark Mode
 */
private val DarkPrimary = Color(0xFFD0BCFF)
private val DarkOnPrimary = Color(0xFF381E72)
private val DarkPrimaryContainer = Color(0xFF4F378B)
private val DarkOnPrimaryContainer = Color(0xFFEADDFF)
private val DarkSecondary = Color(0xFFEFB8C8)
private val DarkOnSecondary = Color(0xFF492532)
private val DarkSecondaryContainer = Color(0xFF633B48)
private val DarkOnSecondaryContainer = Color(0xFFFFD8EE)
private val DarkTertiary = Color(0xFFFFB77C)
private val DarkOnTertiary = Color(0xFF4A2800)
private val DarkTertiaryContainer = Color(0xFF6A3C00)
private val DarkOnTertiaryContainer = Color(0xFFFFE0C2)
private val DarkError = Color(0xFFFFB4AB)
private val DarkOnError = Color(0xFF690005)
private val DarkErrorContainer = Color(0xFF93000A)
private val DarkOnErrorContainer = Color(0xFFFFDAD6)
private val DarkBackground = Color(0xFF1C1B1F)
private val DarkOnBackground = Color(0xFFE6E1E5)
private val DarkSurface = Color(0xFF1C1B1F)
private val DarkOnSurface = Color(0xFFE6E1E5)
private val DarkSurfaceVariant = Color(0xFF49454F)
private val DarkOnSurfaceVariant = Color(0xFFCAC4D0)
private val DarkOutline = Color(0xFF938F99)
private val DarkOutlineVariant = Color(0xFF49454F)
private val DarkInverseSurface = Color(0xFFE6E1E5)
private val DarkInverseOnSurface = Color(0xFF313033)
private val DarkInversePrimary = Color(0xFF7C5CBF)
private val DarkSurfaceTint = Color(0xFFD0BCFF)
private val DarkSurfaceContainer = Color(0xFF211F26)
private val DarkSurfaceContainerHigh = Color(0xFF2B2930)
private val DarkSurfaceContainerHighest = Color(0xFF36343B)
private val DarkSurfaceContainerLow = Color(0xFF1D1B20)
private val DarkSurfaceContainerLowest = Color(0xFF0F0D13)

fun thenaLightColorScheme() = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
    surfaceTint = LightSurfaceTint,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainerLowest = LightSurfaceContainerLowest,
)

fun thenaDarkColorScheme() = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,
    surfaceTint = DarkSurfaceTint,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
)