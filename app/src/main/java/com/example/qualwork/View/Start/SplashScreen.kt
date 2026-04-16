package com.example.qualwork.View.Start

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.SplashDestination
import com.example.qualwork.ViewModel.UserViewModel

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCreateProfile: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        val result = viewModel.checkUser()

        when (result) {
            SplashDestination.Home -> onNavigateToHome()
            SplashDestination.CreateProfile -> onNavigateToCreateProfile()
        }
    }
    QualWorkTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}