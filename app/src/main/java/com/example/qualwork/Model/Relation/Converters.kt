package com.example.qualwork.Model.Relation

import androidx.room.TypeConverter
import com.example.qualwork.Model.Entity.MedicationForm

class Converters {
    @TypeConverter
    fun fromForm(form: MedicationForm): String = form.name

    @TypeConverter
    fun toForm(name: String): MedicationForm = MedicationForm.valueOf(name)
}