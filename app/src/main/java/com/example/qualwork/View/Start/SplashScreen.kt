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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qualwork.Model.UserPreferences
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.SplashDestination
import com.example.qualwork.ViewModel.UserViewModel
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCreateProfile: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val result = viewModel.checkUser()

        viewModel.currentUser?.id?.let { userId ->
            viewModel.syncOnStartup(context, userId)
        }

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