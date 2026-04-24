package com.example.qualwork.ViewModel

import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatTime(time: LocalTime): String =
    time.format(DateTimeFormatter.ofPattern("HH:mm"))

fun formatDoseTime(timestamp: Long): String =
    SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault())
        .format(Date(timestamp))