package com.example.qualwork.ViewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualwork.Data.Model.Medicine
import com.example.qualwork.Data.Model.Pharmacy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.qualwork.Data.Repository.DataScraper
import com.example.qualwork.Data.Repository.LocationHelper

sealed class MedicineSearchState {
    object Idle : MedicineSearchState()
    object Loading : MedicineSearchState()
    data class Success(val medicines: List<Medicine>) : MedicineSearchState()
    data class Error(val message: String) : MedicineSearchState()
}
sealed class MedicineDetailUiState {
    object Loading : MedicineDetailUiState()
    data class Success(val medicine: Medicine) : MedicineDetailUiState()
    data class Error(val message: String) : MedicineDetailUiState()
}
enum class SortType {
    BY_DISTANCE,
    BY_PRICE
}
class MyViewModel(application: Application): AndroidViewModel(application) {
    private val _searchState  = MutableStateFlow<MedicineSearchState>(MedicineSearchState.Idle)
    val searchState: StateFlow<MedicineSearchState> = _searchState.asStateFlow()
    fun search(query: String) {
        if (query.isBlank()) return

        android.util.Log.d("MedicineVM", "Пошук: $query")

        viewModelScope.launch {
            _searchState.value = MedicineSearchState.Loading
            _searchState.value = try {
                val results = DataScraper.search(query)
                if (results.isEmpty()) {
                    MedicineSearchState.Error("Нічого не знайдено")
                } else {
                    MedicineSearchState.Success(results)
                }
            } catch (e: Exception) {
                MedicineSearchState.Error("Помилка мережі: ${e.message}")
            }
        }
    }

    private val _medInfoState  = MutableStateFlow<MedicineDetailUiState>(MedicineDetailUiState.Loading)
    val medInfoState: StateFlow<MedicineDetailUiState> = _medInfoState .asStateFlow()

    private val _sortType = MutableStateFlow(SortType.BY_DISTANCE)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    // Зберігаємо оригінальний список аптек
    private val _allPharmacies = MutableStateFlow<List<Pharmacy>>(emptyList())
    val allPharmacies: StateFlow<List<Pharmacy>> = _allPharmacies.asStateFlow()
    private var currentDetails: Medicine? = null

    fun load(medicineUrl: String, context: Context) {
        viewModelScope.launch {
            _medInfoState .value = MedicineDetailUiState.Loading
            _medInfoState .value = try {
                val userLocation = LocationHelper.getUserLocation(context)
                val details = DataScraper.getMedicineInfo(
                    medicineUrl = medicineUrl,
                    userLat = userLocation?.first,
                    userLon = userLocation?.second
                )

                _allPharmacies.value = if (userLocation != null) {
                    val (userLat, userLon) = userLocation
                    details.pharmacies.map { pharmacy ->
                        pharmacy.copy(
                            distanceKm = DataScraper.calculateDistance(
                                userLat, userLon,
                                pharmacy.latitude, pharmacy.longitude
                            )
                        )
                    }
                } else {
                    details.pharmacies
                }

                currentDetails = details
                MedicineDetailUiState.Success(
                    details.copy(pharmacies = sortPharmacies(_allPharmacies.value, _sortType.value))
                )
            } catch (e: Exception) {
                MedicineDetailUiState.Error("Помилка: ${e.message}")
            }
        }
    }

    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
        val details = currentDetails ?: return
        _medInfoState.value = MedicineDetailUiState.Success(
            details.copy(pharmacies = sortPharmacies(_allPharmacies.value, sortType))
        )
    }
    private fun sortPharmacies(pharmacies: List<Pharmacy>, sortType: SortType): List<Pharmacy> {
        return when (sortType) {
            SortType.BY_DISTANCE -> pharmacies.sortedBy { it.distanceKm }
            SortType.BY_PRICE -> pharmacies.sortedBy {
                it.price.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: Double.MAX_VALUE
            }
        }
    }



    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    fun updateQuery(query: String) {
        _searchQuery.value = query
    }
}