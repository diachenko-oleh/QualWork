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
    pendingDoseTime: Long?,
    onIntakeHandled: () -> Unit
) {
    val navController = rememberNavController()

    LaunchedEffect(pendingIntakeId, pendingDoseTime) {
        val id = pendingIntakeId
        val doseTime = pendingDoseTime
        Log.d("INTAKE_DEBUG", "LaunchedEffect: id=$id, dose=$doseTime")
        if (id != null && doseTime != null) {

            val now = System.currentTimeMillis()
            Log.d("INTAKE_DEBUG", "now=$now, dose=$doseTime, diff=${doseTime - now}ms, isValid=${now <= doseTime + 10*60*1000}")
            val isValid = now <= doseTime + 10 * 60 * 1000 //10хвилин

            if (isValid) {
                navController.navigate(RootNavigator.Home.route) {
                    popUpTo(0)
                }
                navController.navigate(
                    TreatTabNavigator.Intake.createRoute(id, doseTime)
                )
            } else {
                Log.d("INTAKE_DEBUG", "Intake expired, navigation blocked")
            }

            onIntakeHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = RootNavigator.SplashScreen.route
    ) {
        composable(
            route = TreatTabNavigator.Intake.route,
            arguments = listOf(
                navArgument("scheduleId") { type = NavType.LongType },
                navArgument("doseTime") { type = NavType.LongType }
            )
        )
        { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: return@composable
            val doseTime = backStackEntry.arguments?.getLong("doseTime")?: return@composable
            IntakeScreen(
                scheduleId = scheduleId,
                doseTime = doseTime,
                onActionCompleted = { navController.popBackStack() }
            )
        }

        composable(RootNavigator.SplashScreen.route) {
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
            val doseTime = System.currentTimeMillis()
            CourseInfoScreen(
                courseId = courseId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { medicationId ->
                    navController.navigate(TreatTabNavigator.NewCourse.createRoute(medicationId))
                },
                onIntakeClick = { scheduleId, doseTime ->
                    navController.navigate(
                        TreatTabNavigator.Intake.createRoute(scheduleId, doseTime)
                    )
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
    }
}