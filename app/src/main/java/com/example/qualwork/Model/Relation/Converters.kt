package com.example.qualwork.Model.Relation

import androidx.room.TypeConverter
import com.example.qualwork.Model.Entity.MedicationForm
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromForm(form: MedicationForm): String = form.name

    @TypeConverter
    fun toForm(name: String): MedicationForm = MedicationForm.valueOf(name)

    @TypeConverter
    fun fromLocalTime(time: LocalTime): String {
        return time.toString()
    }

    @TypeConverter
    fun toLocalTime(value: String): LocalTime {
        return LocalTime.parse(value)
    }
}