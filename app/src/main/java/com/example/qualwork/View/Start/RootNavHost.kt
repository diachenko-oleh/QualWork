package com.example.qualwork.View.Start

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qualwork.View.QualWorkApp

@Composable
fun RootNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = RootNavigator.SplashScreen.route
    ) {
        composable(RootNavigator.SplashScreen.route) {
            Log.d("NAV", "SplashScreen entered")
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(RootNavigator.Home.route) {
                        popUpTo(RootNavigator.SplashScreen.route) { inclusive = true }
                    }
                },
                onNavigateToCreateProfile = {
                    navController.navigate(RootNavigator.ProfileCreate.route) {
                        popUpTo(RootNavigator.SplashScreen.route) { inclusive = true }
                    }
                }
            )
        }

        composable(RootNavigator.ProfileCreate.route) {
            CreateProfileScreen(
                onProfileCreated = {
                    navController.navigate(RootNavigator.Home.route) {
                        popUpTo(RootNavigator.ProfileCreate.route) { inclusive = true }
                    }
                }
            )
        }

        composable(RootNavigator.Home.route) {
            QualWorkApp()
        }
    }
}