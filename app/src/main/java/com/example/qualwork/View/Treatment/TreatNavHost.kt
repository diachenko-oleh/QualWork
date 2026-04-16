package com.example.qualwork.View.Treatment

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qualwork.View.Treatment.TreatTabNavigator
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.MyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentScreen() {
    QualWorkTheme {
        val navController = rememberNavController()
        val viewModel: MyViewModel = viewModel()
        NavHost(
            navController = navController,
            startDestination = TreatTabNavigator.TreatMain.route,
        ){
            composable(TreatTabNavigator.TreatMain.route) {
                TreatMainPage({ navController.navigate(TreatTabNavigator.NewCourse.route) })
            }
            composable(TreatTabNavigator.NewCourse.route){
                NewCourse(
                    onCourseAdded = { navController.popBackStack() }
                )
            }
        }
    }
}
