package com.example.qualwork.View.Treatment

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.qualwork.View.theme.QualWorkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentScreen(
    navController: NavHostController
) {
    QualWorkTheme {
        TreatMainPage(
            onAddCourseClick = { navController.navigate(TreatTabNavigator.NewCourse.route) },
            onCourseClick = { courseId ->
                navController.navigate("courseInfoScreen/$courseId")
            }
        )
    }
}
