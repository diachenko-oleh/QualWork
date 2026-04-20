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
            composable(
                route = TreatTabNavigator.NewCourse.route,
                arguments = listOf(
                    navArgument("courseId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getLong("courseId")
                    ?.takeIf { it != -1L }
                NewCourse(
                    courseId = courseId,
                    onBackClick = { navController.popBackStack() },
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
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { medicationId ->
                        navController.navigate(TreatTabNavigator.NewCourse.createRoute(medicationId))
                    },
                    onIntakeClick = { scheduleId ->
                        navController.navigate(TreatTabNavigator.Intake.createRoute(scheduleId))
                    }
                )
            }
            composable(
                route = TreatTabNavigator.Intake.route,
                arguments = listOf(
                    navArgument("scheduleId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: 0L
                IntakeScreen(
                    scheduleId = scheduleId,
                    onActionCompleted = { navController.popBackStack() }
                )
            }
        }
    }
}
