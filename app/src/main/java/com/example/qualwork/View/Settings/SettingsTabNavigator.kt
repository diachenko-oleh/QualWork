package com.example.qualwork.View.Settings

sealed class SettingsTabNavigator(val route: String) {
    object SettingsMain : SettingsTabNavigator("settingsMain")
    object Account : SettingsTabNavigator("accountScreen")
}