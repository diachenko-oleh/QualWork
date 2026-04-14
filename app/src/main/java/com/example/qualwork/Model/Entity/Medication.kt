package com.example.qualwork.Model.Entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MedicationForm(val displayName: String, val unit: String) {
    TABLET("Таблетки", "таблетки"),
    CAPSULE("Капсули", "капсули"),
    SPRAY("Спрей", "впорскування"),
    DROPS("Краплі", "краплі"),
    SYRUP("Сироп", "мл"),
    POWDER("Порошок", "пакетики")
}

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    //@ColumnInfo(name = "form")
    val form: MedicationForm
)