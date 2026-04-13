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
sealed class MedicineInfoUiState {
    object Loading : MedicineInfoUiState()
    data class Success(val medicine: Medicine) : MedicineInfoUiState()
    data class Error(val message: String) : MedicineInfoUiState()
}
data class FilterState(
    val minPrice: Float = 0f,
    val maxPrice: Float = 0f,
    val maxPriceLimit: Float = 0f,
    val onlyAvailable: Boolean = false
)
enum class SortType {
    BY_DISTANCE,
    BY_PRICE
}

class MyViewModel(application: Application): AndroidViewModel(application) {
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()
    private var originalMedicines: List<Medicine> = emptyList() // Оригінальний список без фільтрів

    private val _searchState  = MutableStateFlow<MedicineSearchState>(MedicineSearchState.Idle)
    val searchState: StateFlow<MedicineSearchState> = _searchState.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) return

        android.util.Log.d("MedicineVM", "Пошук: $query")

        viewModelScope.launch {
            _searchState.value = MedicineSearchState.Loading
            _filterState.value = FilterState()
            _searchState.value = try {
                val results = DataScraper.search(query)
                originalMedicines = results
                val maxPrice = results.maxOfOrNull { medicine ->
                    medicine.minPrice
                        .replace("[^\\d.]".toRegex(), "")
                        .toFloatOrNull() ?: 0f
                } ?: 9999f

                // Встановлюємо фільтр з динамічним діапазоном
                _filterState.value = FilterState(
                    minPrice = 0f,
                    maxPrice = maxPrice,
                    maxPriceLimit = maxPrice
                )
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

    fun applyFilters(minPrice: Float, maxPrice: Float,maxPriceLimit:Float, onlyAvailable: Boolean) {
        _filterState.value = FilterState(minPrice, maxPrice,maxPriceLimit,  onlyAvailable)

        val filtered = originalMedicines.filter { medicine ->
            val price = medicine.minPrice
                .replace("[^\\d.]".toRegex(), "")
                .toFloatOrNull() ?: 0f

            val priceOk = price >= minPrice && price <= maxPrice
            val availabilityOk = if (onlyAvailable) medicine.pharmacyCount > 0 else true

            priceOk && availabilityOk
        }

        _searchState.value = if (filtered.isEmpty()) {
            MedicineSearchState.Error("Нічого не знайдено за вибраними фільтрами")
        } else {
            MedicineSearchState.Success(filtered)
        }
    }
    private val _medInfoState  = MutableStateFlow<MedicineInfoUiState>(MedicineInfoUiState.Loading)
    val medInfoState: StateFlow<MedicineInfoUiState> = _medInfoState .asStateFlow()

    private val _sortType = MutableStateFlow(SortType.BY_DISTANCE)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _allPharmacies = MutableStateFlow<List<Pharmacy>>(emptyList())
    val allPharmacies: StateFlow<List<Pharmacy>> = _allPharmacies.asStateFlow()
    private var currentDetails: Medicine? = null

    fun getAllPharmacies(medicineUrl: String, context: Context) {
        viewModelScope.launch {
            _medInfoState .value = MedicineInfoUiState.Loading
            _medInfoState .value = try {
                val userLocation = LocationHelper.getUserLocation(context)
                val details = DataScraper.getMedicineInfo(
                    medicineUrl = medicineUrl,
                    userLat = userLocation?.first,
                    userLon = userLocation?.second
                )

                val eLikyStatus = DataScraper.checkELiky(details.name)
                android.util.Log.d("ELIKY", "status: $eLikyStatus")

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
                MedicineInfoUiState.Success(
                    details.copy(
                        pharmacies = sortPharmacies(_allPharmacies.value, _sortType.value),
                        eLikyStatus = eLikyStatus
                    )
                )
            } catch (e: Exception) {
                MedicineInfoUiState.Error("Помилка: ${e.message}")
            }
        }
    }

    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
        val details = currentDetails ?: return
        _medInfoState.value = MedicineInfoUiState.Success(
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