package com.example.qualwork.Model.Relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.qualwork.Model.Entity.Medication
import com.example.qualwork.Model.Entity.Schedule

data class MedicationWithSchedules(
    @Embedded val medication: Medication,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicationId"
    )
    val schedules: List<Schedule>
)