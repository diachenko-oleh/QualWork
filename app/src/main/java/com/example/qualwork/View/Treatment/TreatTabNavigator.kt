package com.example.qualwork.View.Treatment

sealed class TreatTabNavigator(val route: String) {
    object TreatMain : TreatTabNavigator("treatmentMain")
    object NewCourse : TreatTabNavigator("newCourseScreen?courseId={courseId}") {
        fun createRoute(courseId: Long? = null) =
            if (courseId != null) "newCourseScreen?courseId=$courseId"
            else "newCourseScreen"
    }
    object TreatInfo : TreatTabNavigator("courseInfoScreen/{courseId}")

}