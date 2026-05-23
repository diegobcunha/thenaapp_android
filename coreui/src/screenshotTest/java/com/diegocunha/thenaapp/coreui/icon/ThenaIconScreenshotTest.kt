package com.diegocunha.thenaapp.coreui.icon

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
fun ThenaIconVectorLightPreview() {
    ThenaTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            ThenaIcon(icon = ThenaIcons.Sleep)
            ThenaIcon(icon = ThenaIcons.Vaccine)
            ThenaIcon(icon = ThenaIcons.Statistics)
            ThenaIcon(icon = ThenaIcons.Celebration)
            ThenaIcon(icon = ThenaIcons.Timer)
            ThenaIcon(icon = ThenaIcons.Female)
            ThenaIcon(icon = ThenaIcons.Male)
            ThenaIcon(icon = ThenaIcons.People)
            ThenaIcon(icon = ThenaIcons.CarNap)
            ThenaIcon(icon = ThenaIcons.EarlyMorning)
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun ThenaIconVectorDarkPreview() {
    ThenaTheme(darkTheme = true) {
        Column(modifier = Modifier.fillMaxSize()) {
            ThenaIcon(icon = ThenaIcons.Sleep)
            ThenaIcon(icon = ThenaIcons.Vaccine)
            ThenaIcon(icon = ThenaIcons.Statistics)
            ThenaIcon(icon = ThenaIcons.Celebration)
            ThenaIcon(icon = ThenaIcons.Timer)
            ThenaIcon(icon = ThenaIcons.Female)
            ThenaIcon(icon = ThenaIcons.Male)
            ThenaIcon(icon = ThenaIcons.People)
            ThenaIcon(icon = ThenaIcons.CarNap)
            ThenaIcon(icon = ThenaIcons.EarlyMorning)
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun ThenaIconDrawableLightPreview() {
    ThenaTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            ThenaIcon(icon = ThenaIcons.Bottle)
            ThenaIcon(icon = ThenaIcons.Breastfeeding)
            ThenaIcon(icon = ThenaIcons.Blossom)
            ThenaIcon(icon = ThenaIcons.Nap)
            ThenaIcon(icon = ThenaIcons.Catnap)
        }
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun ThenaIconDrawableDarkPreview() {
    ThenaTheme(darkTheme = true) {
        Column(modifier = Modifier.fillMaxSize()) {
            ThenaIcon(icon = ThenaIcons.Bottle)
            ThenaIcon(icon = ThenaIcons.Breastfeeding)
            ThenaIcon(icon = ThenaIcons.Blossom)
            ThenaIcon(icon = ThenaIcons.Nap)
            ThenaIcon(icon = ThenaIcons.Catnap)
        }
    }
}