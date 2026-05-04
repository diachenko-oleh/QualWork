package com.example.qualwork.View.Search

import android.Manifest
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.qualwork.Model.Repository.LocationHelper
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMainPage(viewModel: SearchViewModel, openSearchBarScreen: () -> Unit){
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
                horizontalAlignment = Alignment.Start,
            ) {
                LocationInfoCard(
                    isPermissionGranted = isPermissionGranted,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
                SearchButton(openSearchBarScreen)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchButton(openSearchBarScreen:() -> Unit) {
    Column(
       modifier = Modifier
           .padding(horizontal = 16.dp)
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
            enabled = false,
            windowInsets = WindowInsets(top = 0.dp)
        ) {}
    }
}
@Composable
fun LocationInfoCard(
    isPermissionGranted: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var city by remember { mutableStateOf("Визначення...") }
    var fullAddress by remember { mutableStateOf("") }
    var coordinates by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(isPermissionGranted) {
        if (!isPermissionGranted) {
            city = "Локацію не визначено"
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            val location = LocationHelper.getUserLocation(context)
            if (location != null) {
                val (lat, lon) = location
                coordinates = "${"%.6f".format(lat)}, ${"%.6f".format(lon)}"
                try {
                    val geocoder = Geocoder(context, Locale("uk"))
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    val address = addresses?.firstOrNull()
                    city = address?.locality
                        ?: address?.subAdminArea
                                ?: address?.adminArea
                                ?: "Місто невідоме"
                    fullAddress = address?.getAddressLine(0) ?: coordinates
                } catch (e: Exception) {
                    city = "Місто невідоме"
                    fullAddress = coordinates
                }
            } else {
                city = "Локацію не визначено"
            }
        }
    }

    Card(
        onClick = { if (fullAddress.isNotEmpty()) isExpanded = !isExpanded },
        modifier = modifier.wrapContentWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { expanded ->
            if (!expanded) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = city,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = city,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = fullAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
