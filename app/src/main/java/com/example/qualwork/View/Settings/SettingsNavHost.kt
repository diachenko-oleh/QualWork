package com.example.qualwork.View.Settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    QualWorkTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = SettingsTabNavigator.Account.route,
        ){
            composable(SettingsTabNavigator.Account.route) {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() },
                )
            }
        }
    }
}