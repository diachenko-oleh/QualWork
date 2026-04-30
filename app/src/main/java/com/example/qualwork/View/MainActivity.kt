package com.example.qualwork.View

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.qualwork.Model.DAO.IntakeLogDao
import com.example.qualwork.View.Settings.SettingsScreen
import com.example.qualwork.View.Search.SearchScreen
import com.example.qualwork.View.Start.RootNavHost
import com.example.qualwork.View.Treatment.TreatmentScreen
import com.example.qualwork.View.theme.QualWorkTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var intakeLogDao: IntakeLogDao
    private var intakeScheduleId by mutableStateOf<Long?>(null)
    private var doseTime by mutableStateOf<Long?>(null)

    private fun handleIntent(intent: Intent) {
        val id = intent.getLongExtra("scheduleId", -1L)
        val timeString = intent.getStringExtra("time")

        Log.d("INTAKE_DEBUG", "handleIntent: id=$id, timeString=$timeString")

        if (id != -1L && timeString != null) {
            val time = LocalTime.parse(timeString)
            val doseDateTime = LocalDate.now().atTime(time)
            val epochMilli = doseDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            Log.d("INTAKE_DEBUG", "Converted: LocalTime=$time, epochMilli=$epochMilli")
            lifecycleScope.launch {
                val plannedDate = doseDateTime.toString().substring(0, 16)
                val alreadyTaken = intakeLogDao.isTaken(id, plannedDate) > 0

                if (!alreadyTaken) {
                    intakeScheduleId = id
                    doseTime = epochMilli
                } else {
                    Log.d("INTAKE_DEBUG", "Already taken, skip navigation")
                }
            }
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Log.d("Notifications", "Дозвіл відхилено")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            QualWorkTheme {
                RootNavHost(
                    pendingIntakeId = intakeScheduleId,
                    pendingDoseTime = doseTime,
                    onIntakeHandled = { intakeScheduleId = null }
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!isGranted) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
}
@Composable
fun QualWorkApp(
    navController: NavHostController
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.TREATMENT) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
        ),
        layoutType = if (currentRoute  == "newCourseScreen") {
            NavigationSuiteType.None
        } else {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
                currentWindowAdaptiveInfo()
            )
        }
    ) {
        when (currentDestination) {
            AppDestinations.SEARCH -> SearchScreen()
            AppDestinations.TREATMENT -> TreatmentScreen(
                navController = navController
            )
            AppDestinations.PROFILE -> SettingsScreen()

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


