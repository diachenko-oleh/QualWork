package com.example.qualwork.Model.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.qualwork.Model.Entity.IntakeTimeEntity

@Dao
interface IntakeTimeDao {

    @Query("SELECT * FROM IntakeTimeEntity WHERE scheduleId = :scheduleId")
    suspend fun getTimesForSchedule(scheduleId: Long): List<IntakeTimeEntity>
    @Query("SELECT * FROM IntakeTimeEntity WHERE scheduleId = :scheduleId")
    suspend fun getBySchedule(scheduleId: Long): List<IntakeTimeEntity>
    @Insert
    suspend fun insertAll(times: List<IntakeTimeEntity>)

    @Query("DELETE FROM IntakeTimeEntity WHERE scheduleId = :scheduleId")
    suspend fun deleteBySchedule(scheduleId: Long)
}