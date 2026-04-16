package com.example.qualwork.Model.Relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.qualwork.Model.Entity.Connection
import com.example.qualwork.Model.Entity.User

data class UserWithSupervisors(
    @Embedded val user: User,

    @Relation(
        parentColumn = "id",
        entityColumn = "supervisorId"
    )
    val supervisors: List<Connection>
)