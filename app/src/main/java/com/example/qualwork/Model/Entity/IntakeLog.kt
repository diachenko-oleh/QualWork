package com.example.qualwork.Model.Entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intake_logs",
    foreignKeys = [
        ForeignKey(
            entity = Schedule::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scheduleId")]
)
data class IntakeLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleId: Long,
    val intakeTime: Long,
    val doseTime: Long,
    val taken: Boolean
)