package com.example.qualwork.View.Start

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.qualwork.View.QualWorkApp
import com.example.qualwork.View.Treatment.CourseInfoScreen
import com.example.qualwork.View.Treatment.IntakeScreen
import com.example.qualwork.View.Treatment.NewCourse
import com.example.qualwork.View.Treatment.TreatTabNavigator
import kotlinx.coroutines.delay

@Composable
fun RootNavHost(
    pendingIntakeId: Long?,
    onIntakeHandled: () -> Unit
) {
    Log.d("NOTIF_FLOW", "RootNavHost intakeScheduleId = $pendingIntakeId")
    val navController = rememberNavController()
    LaunchedEffect(pendingIntakeId) {
        pendingIntakeId?.let { id ->

            navController.navigate(RootNavigator.Home.route) {
                popUpTo(0)
            }

            navController.navigate(
                TreatTabNavigator.Intake.createRoute(id)
            )

            onIntakeHandled()
        }
    }

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
            QualWorkApp(
                navController = navController
            )

        }

        composable(
            route= TreatTabNavigator.TreatInfo.route,
            arguments = listOf(navArgument("courseId") { type = NavType.LongType })
        )
        {backStackEntry ->
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
            route = TreatTabNavigator.NewCourse.route,
            arguments = listOf(
                navArgument("courseId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        )
        { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong("courseId")
                ?.takeIf { it != -1L }
            NewCourse(
                courseId = courseId,
                onBackClick = { navController.popBackStack() },
                onCourseAdded = { navController.popBackStack() }
            )
        }

        composable(
            route = TreatTabNavigator.Intake.route,
            arguments = listOf(navArgument("scheduleId") { type = NavType.LongType }
            )
        )
        { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: 0L
            IntakeScreen(
                scheduleId = scheduleId,
                onActionCompleted = { navController.popBackStack() }
            )
        }
    }
}