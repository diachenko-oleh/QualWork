package com.example.qualwork.View

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.qualwork.View.Search.SearchScreen
import com.example.qualwork.View.theme.QualWorkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QualWorkTheme {
                QualWorkApp()
            }
        }
    }
}
@PreviewScreenSizes
@Composable
fun QualWorkApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.TREATMENT) }
    val itemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Red,
            selectedTextColor = Color.Red,
            indicatorColor = MaterialTheme.colorScheme.background,
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray,

        )
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it },
                    colors = itemColors,
                )
            }
        },
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.background,
        )
    ) {
        when (currentDestination) {
            AppDestinations.SEARCH -> SearchScreen()
            AppDestinations.TREATMENT -> TreatmentScreen()
            AppDestinations.PROFILE -> TODO()

        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    SEARCH("Пошук",Icons.Default.Search),
    TREATMENT("Курси лікування", Icons.Default.Medication),
    PROFILE("Налаштування", Icons.Default.Settings),;
}


