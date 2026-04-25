package com.diegocunha.thenaapp.coreui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.diegocunha.thenaapp.coreui.R
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme

@Composable
fun LoadingComponent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(ThenaTheme.spacing.m),
            strokeWidth = ThenaTheme.spacing.xxs,
            color = ThenaTheme.colors.onPrimary,
        )
        Spacer(
            modifier = Modifier.height(ThenaTheme.spacing.sm)
        )

        Text(
            text = stringResource(R.string.loading)
        )
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    ThenaTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LoadingComponent()
        }
    }
}
