package com.example.qualwork.View.Start

sealed class RootNavigator(val route: String) {
    object SplashScreen : RootNavigator("splash")
    object ProfileCreate : RootNavigator("profile_create")
    object Home : RootNavigator("home")
}