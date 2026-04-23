package com.example.qualwork.View.Treatment

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun intervalLabel(hours: Int): String = when (hours) {
    1 -> "Щогодини"
    2 -> "Кожні 2 год"
    3 -> "Кожні 3 год"
    4 -> "Кожні 4 год"
    6 -> "Кожні 6 год"
    8 -> "Кожні 8 год"
    12 -> "Двічі на день"
    24 -> "Раз на день"
    else -> "Кожні $hours год"
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}