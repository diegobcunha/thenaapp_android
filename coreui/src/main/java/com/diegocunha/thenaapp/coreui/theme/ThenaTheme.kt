package com.diegocunha.thenaapp.coreui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ThenaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) thenaDarkColorScheme() else thenaLightColorScheme()
    val extendedColors = if (darkTheme) thenaDarkExtendedColors else thenaLightExtendedColors

    CompositionLocalProvider(LocalThenaExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = thenaTypography(),
            shapes = thenaShapes(),
            content = content,
        )
    }
}

object ThenaTheme {
    val colors: ColorScheme
        @Composable get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable get() = MaterialTheme.shapes

    val extendedColors: ThenaExtendedColors
        @Composable get() = LocalThenaExtendedColors.current
}

@Preview(name = "ThenaTheme — Light", showBackground = true)
@Composable
private fun ThenaThemeLightPreview() {
    ThenaTheme(darkTheme = false) {
        Surface(color = ThenaTheme.colors.background) {
            Button(onClick = {}) {
                Text("Thena light theme")
            }
        }
    }
}

@Preview(name = "ThenaTheme — Dark", showBackground = true)
@Composable
private fun ThenaThemeDarkPreview() {
    ThenaTheme(darkTheme = true) {
        Surface(color = ThenaTheme.colors.background) {
            Button(onClick = {}) {
                Text("Thena dark theme")
            }
        }
    }
}