package com.example.qualwork.View.Search

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.qualwork.Model.Entity.Pharmacy
import com.example.qualwork.Model.Repository.SocialProgramStatus
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.MedicineInfoUiState
import com.example.qualwork.ViewModel.SearchViewModel
import com.example.qualwork.ViewModel.SortType
import androidx.core.net.toUri


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedInfoPage(viewModel: SearchViewModel, onBack: () -> Unit, medicineUrl: String) {
    val context = LocalContext.current
    val sortType by viewModel.sortType.collectAsState()
    val medInfoState by viewModel.medInfoState.collectAsState()
    val allPharmacies by viewModel.allPharmacies.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.getAllPharmacies(medicineUrl, context)
        }
    }
    LaunchedEffect(medicineUrl) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    QualWorkTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = when (val state = medInfoState) {
                                is MedicineInfoUiState.Success -> state.searchMedication.name
                                is MedicineInfoUiState.Loading -> "Завантаження..."
                                is MedicineInfoUiState.Error -> "Помилка"
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            when (val state = medInfoState) {
                is MedicineInfoUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is MedicineInfoUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }

                is MedicineInfoUiState.Success -> {
                    val info = state.searchMedication
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            AsyncImage(
                                model = info.imageUrl,
                                contentDescription = info.name,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.FillWidth,

                                )
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    android.util.Log.d("MED_INFO_CHECK", "manufacturer: ${info.manufacturer}")
                                    Text(
                                        text = info.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = info.manufacturer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = info.minPrice,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        item {
                            RecommendationBlock(
                                pharmacies = allPharmacies,
                                socialProgramStatus = state.searchMedication.socialProgramStatus,
                                medicineName = state.searchMedication.name
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = sortType == SortType.BY_DISTANCE,
                                    onClick = { viewModel.setSortType(SortType.BY_DISTANCE) },
                                    label = { Text("За відстанню") },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        labelColor = MaterialTheme.colorScheme.onBackground,
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    )
                                )
                                FilterChip(
                                    selected = sortType == SortType.BY_PRICE,
                                    onClick = { viewModel.setSortType(SortType.BY_PRICE) },
                                    label = { Text("За ціною") },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        labelColor = MaterialTheme.colorScheme.onBackground,
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    )
                                )
                            }
                        }
                        item {
                            Text(
                                text = "Аптеки (${info.pharmacies.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            HorizontalDivider()
                        }
                        items(info.pharmacies) { pharmacy ->
                            PharmacyCard(pharmacy)
                        }
                        item{
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                    }
                }
            }
        }
    }
}
@Composable
fun RecommendationBlock(
    pharmacies: List<Pharmacy>,
    socialProgramStatus: SocialProgramStatus,
    medicineName: String
) {
    if (pharmacies.isEmpty()) return

    val nearest = pharmacies.minByOrNull { it.distanceKm }
    val cheapest = pharmacies.minByOrNull {
        it.price.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: Double.MAX_VALUE
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Рекомендації",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            nearest?.let {
                Text(
                    text = "Найближча аптека",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = it.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = it.price,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${"%.1f".format(it.distanceKm)} км від вас",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (nearest?.name != cheapest?.name) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                cheapest?.let {
                    Text(
                        text = "Найнижча ціна",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = it.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = it.price,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${"%.1f".format(it.distanceKm)} км від вас",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ця аптека є і найближчою і найвигіднішою!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Пільгова наявність",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))

            val context = LocalContext.current
            when (socialProgramStatus) {
                SocialProgramStatus.AVAILABLE -> {
                    Text(
                        text = "Препарат можна отримати через програму \"Доступні ліки\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            val searchQuery = medicineName.split(" ", "-").first().trim()
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://likicontrol.com.ua/пошук-ліків/?$searchQuery".toUri()
                            )
                            context.startActivity(intent)
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Детальніше на likicontrol.com.ua...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                SocialProgramStatus.NOT_AVAILABLE -> {
                    Text(
                        text = "Препарат не входить до програми \"Доступні ліки\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                SocialProgramStatus.NOT_FOUND -> {
                    Text(
                        text = "Інформація відсутня",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
@Composable
fun PharmacyCard(pharmacy: Pharmacy) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = pharmacy.name,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = pharmacy.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = pharmacy.price,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (pharmacy.distanceKm > 0)
                        "${"%.1f".format(pharmacy.distanceKm)} км"
                    else
                        "відстань невідома",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


