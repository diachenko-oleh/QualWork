package com.example.qualwork.Model.Relation

import androidx.room.TypeConverter
import com.example.qualwork.Model.Entity.MedicationForm
import java.time.LocalDateTime
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromForm(form: MedicationForm): String = form.name

    @TypeConverter
    fun toForm(name: String): MedicationForm = MedicationForm.valueOf(name)

    @TypeConverter
    fun fromLocalDateTime(time: LocalDateTime): String {
        return time.toString()
    }

    @TypeConverter
    fun toLocalDateTime(value: String): LocalDateTime {
        return LocalDateTime.parse(value)
    }
}