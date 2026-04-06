package com.example.qualwork.View


import android.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.MyViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
                    openMedInfo = { medicine ->
                        val encodedUrl = URLEncoder.encode(
                            medicine.url,
                            StandardCharsets.UTF_8.toString()
                        )
                        navController.navigate("medPage/$encodedUrl")
                    },
                    onBack = {navController.navigateUp() }
                )
            }

            composable(
                SearchTabNavigator.MedInfo.route,
                arguments = listOf(
                    navArgument("url") { type = NavType.StringType }
                )
            ) {backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
                val medicineUrl = URLDecoder.decode(
                    encodedUrl,
                    StandardCharsets.UTF_8.toString()
                )
                MedInfoPage(
                    viewModel,
                    onBack = { navController.navigateUp() },
                    medicineUrl = medicineUrl
                )
            }
        }
    }
}
