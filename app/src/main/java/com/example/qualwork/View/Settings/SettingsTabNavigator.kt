package com.example.qualwork.View.Settings

sealed class SettingsTabNavigator(val route: String) {
    object Account : SettingsTabNavigator("accountScreen")
}