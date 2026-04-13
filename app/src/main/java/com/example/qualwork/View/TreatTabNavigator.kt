package com.example.qualwork.View

sealed class TreatTabNavigator(val route: String) {
    object TreatMain : TreatTabNavigator("treatmentMain")
    object NewCourse : TreatTabNavigator("newCourseScreen")
    object TreatInfo : TreatTabNavigator("treatPage/{url}")

}