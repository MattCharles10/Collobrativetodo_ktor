package com.example.collaborativetodo.ui.navigation

import androidx.compose.runtime.Composable
import com.example.collaborativetodo.navigation.NavGraph
import com.example.collaborativetodo.ui.viewmodels.AuthViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavigator() {
    val authViewModel: AuthViewModel = koinInject()
    val isLoggedIn = authViewModel.isLoggedIn()

    NavGraph(
        isLoggedIn = isLoggedIn,
        authViewModel = authViewModel
    )
}