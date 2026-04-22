package com.diegocunha.thenaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.diegocunha.thenaapp.coreui.theme.ThenaTheme
import com.diegocunha.thenaapp.feature.login.presentation.LoginScreen
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThenaTheme {
                LoginScreen(
                    viewModel = koinViewModel(),
                    onNavigateToHome = {},
                )
            }
        }
    }
}