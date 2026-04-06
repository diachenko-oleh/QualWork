package com.example.qualwork.View

sealed class SearchTabNavigator(val route: String) {
    object SearchMain : SearchTabNavigator("searchMain")
    object SearchBarScreen : SearchTabNavigator("searchBarScreen")
    object MedInfo : SearchTabNavigator("medPage/{url}")

}