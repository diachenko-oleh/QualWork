package com.example.qualwork.Model.Relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.qualwork.Model.Entity.IntakeLog
import com.example.qualwork.Model.Entity.Schedule

data class ScheduleWithLogs(
    @Embedded val schedule: Schedule,

    @Relation(
        parentColumn = "id",
        entityColumn = "scheduleId"
    )
    val logs: List<IntakeLog>
)