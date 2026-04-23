package com.example.qualwork.Model.Entity

data class DayIntakeStat(
    val date: String,
    val total: Int,
    val taken: Int
) {
    val missed: Int get() = total - taken
}