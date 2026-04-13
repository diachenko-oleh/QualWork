package com.example.qualwork.View.Search

import android.Manifest
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.qualwork.Data.Repository.LocationHelper
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.MyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMainPage(viewModel: MyViewModel, openSearchBarScreen: () -> Unit){
    var isPermissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isPermissionGranted = isGranted
    }
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    QualWorkTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Пошук медичних препаратів") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
            ) {
                SearchButton(openSearchBarScreen)
                LocationInfoText(isPermissionGranted = isPermissionGranted)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchButton(openSearchBarScreen:() -> Unit) {
    QualWorkTheme {
        Column(
           modifier = Modifier
               .padding(horizontal = 16.dp)
               .padding(bottom = 16.dp)
        ) {
            SearchBar(
                query = "",
                onQueryChange = {},
                onSearch = {},
                active = false,
                onActiveChange = {},
                modifier = Modifier
                    .clickable {openSearchBarScreen()},
                placeholder = { Text("Пошук...") },
                enabled = false
            ) {}
        }
    }
}
@Composable
fun LocationInfoText(isPermissionGranted: Boolean) {
    val context = LocalContext.current
    var locationText by remember { mutableStateOf("Визначення локації...") }

    LaunchedEffect(isPermissionGranted) {
        if (!isPermissionGranted) return@LaunchedEffect
        withContext(Dispatchers.IO) {

            val location = LocationHelper.getUserLocation(context)
            locationText = if (location != null) {
                val (lat, lon) = location
                try {
                    val geocoder = Geocoder(context, Locale("uk"))
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    val address = addresses?.firstOrNull()
                    val addressText = address?.getAddressLine(0) ?: "Адреса невідома"
                    "📍 $addressText\n🌐 ${"%.6f".format(lat)}, ${"%.6f".format(lon)}"
                } catch (e: Exception) {
                    "🌐 ${"%.6f".format(lat)}, ${"%.6f".format(lon)}"
                }
            } else {
                "Локацію не визначено"
            }
        }
    }

    Text(
        text = locationText,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
