package com.diegocunha.thenaapp.coreui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

@Composable
fun CardButtonInformation(
    modifier: Modifier = Modifier,
    color: Color = ThenaTheme.colors.background,
    headerInformation: @Composable () -> Unit,
    titleInformation: @Composable () -> Unit,
    subtitleInformation: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(ThenaTheme.spacing.sm),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ThenaTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ThenaTheme.spacing.xs),
        ) {

            headerInformation()
            titleInformation()
            subtitleInformation?.invoke()

        }
    }
}

@Preview
@Composable
private fun CardButtonInformationPreview() {
    ThenaTheme {
        CardButtonInformation(
            headerInformation = { Text("") },
            titleInformation = { Text("Hello world") },
            subtitleInformation = { Text("Subtitle information") }
        )
    }
}
