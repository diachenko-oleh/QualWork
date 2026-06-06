package com.example.qualwork.Model.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.qualwork.Model.Entity.IntakeTime
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeTimeDao {

    @Query("SELECT * FROM IntakeTime WHERE scheduleId = :scheduleId")
    suspend fun getTimesForSchedule(scheduleId: Long): List<IntakeTime>
    @Query("SELECT * FROM IntakeTime")
    fun observeAll(): Flow<List<IntakeTime>>

    @Query("SELECT * FROM IntakeTime WHERE scheduleId = :scheduleId")
    suspend fun getBySchedule(scheduleId: Long): List<IntakeTime>
    
    @Insert
    suspend fun insertAll(times: List<IntakeTime>): List<Long>

    @Query("DELETE FROM IntakeTime WHERE scheduleId = :scheduleId")
    suspend fun deleteBySchedule(scheduleId: Long)
}