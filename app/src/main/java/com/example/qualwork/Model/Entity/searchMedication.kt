package com.example.qualwork.Model.Entity

import com.example.qualwork.Model.Repository.SocialProgramStatus

data class searchMedication(
    val name: String,
    val manufacturer: String,
    val minPrice: String,
    val url: String,
    val imageUrl: String = "",
    val isExact: Boolean = true,
    val pharmacyCount: Int = 0,
    val pharmacies: List<Pharmacy> = emptyList(),
    val socialProgramStatus: SocialProgramStatus = SocialProgramStatus.NOT_FOUND
)