package com.example.qualwork.Model.Relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.qualwork.Model.Entity.Schedule
import com.example.qualwork.Model.Entity.User

data class UserWithSchedules(
    @Embedded val user: User,

    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val schedules: List<Schedule>
)