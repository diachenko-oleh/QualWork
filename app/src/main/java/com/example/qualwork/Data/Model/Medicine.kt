package com.example.qualwork.Data.Model

import com.example.qualwork.Data.Repository.ELikyStatus

data class Medicine(
    val name: String,
    val manufacturer: String,
    val minPrice: String,
    val url: String,
    val imageUrl: String = "",
    val isExact: Boolean = true,
    val pharmacyCount: Int = 0,
    val pharmacies: List<Pharmacy> = emptyList(),
    val eLikyStatus: ELikyStatus = ELikyStatus.NOT_FOUND
)