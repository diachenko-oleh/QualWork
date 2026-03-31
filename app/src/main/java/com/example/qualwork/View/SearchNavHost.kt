package com.example.qualwork.View


import android.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.MyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    QualWorkTheme {
        val navController = rememberNavController()
        val viewModel: MyViewModel = viewModel()
        NavHost(
            navController = navController,
            startDestination = SearchTabNavigator.SearchMain.route,
        ) {
            composable(SearchTabNavigator.SearchMain.route) {
                SearchMainPage(
                    viewModel,
                    {navController.navigate(SearchTabNavigator.SearchBarScreen.route) }
                )
            }
            composable(SearchTabNavigator.SearchBarScreen.route){
                SearchBarScreen(
                    viewModel,
                    {navController.navigate(SearchTabNavigator.MedInfo.route) },
                    onBack = {navController.navigateUp() }
                )
            }
            composable(SearchTabNavigator.MedInfo.route) {
                MedInfoPage(
                    onBack = { navController.navigateUp() },
                    name = viewModel.searchText.value
                )
            }
        }
    }
}
