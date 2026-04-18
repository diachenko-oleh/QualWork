package com.example.qualwork.View.Treatment

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.qualwork.View.Treatment.TreatTabNavigator
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.MyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentScreen(navController: NavHostController) {
    QualWorkTheme {
        //val navController = rememberNavController()
        val viewModel: MyViewModel = viewModel()
        NavHost(
            navController = navController,
            startDestination = TreatTabNavigator.TreatMain.route,
        ){
            composable(TreatTabNavigator.TreatMain.route) {
                TreatMainPage(
                    onAddCourseClick = { navController.navigate(TreatTabNavigator.NewCourse.route) },
                    onCourseClick = { courseId ->
                        navController.navigate("courseInfoScreen/$courseId")
                    })
            }
            composable(TreatTabNavigator.NewCourse.route){
                NewCourse(
                    onCourseAdded = { navController.popBackStack() }
                )
            }
            composable(
                route= TreatTabNavigator.TreatInfo.route,
                arguments = listOf(navArgument("courseId") { type = NavType.LongType })) {
                    backStackEntry ->
                val courseId = backStackEntry.arguments?.getLong("courseId") ?: 0L
                CourseInfoScreen(
                    courseId = courseId,
                    onBackClick = { navController.popBackStack() }
                )

            }
        }
    }
}
