package com.example.qualwork.Model.Entity

data class Pharmacy(
    val name: String,
    val address: String,
    val price: String,
    val latitude: Double,
    val longitude: Double,
    var distanceKm: Double = 0.0
)