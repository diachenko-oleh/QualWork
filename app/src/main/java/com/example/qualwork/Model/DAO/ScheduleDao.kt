package com.example.qualwork.Model.DAO
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.qualwork.Model.Entity.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule): Long

    @Query("SELECT * FROM schedules WHERE medicationId = :medicationId")
    fun getByMedication(medicationId: Long): Flow<List<Schedule>>

    @Query("""
        SELECT * FROM schedules 
        WHERE (:today BETWEEN startDate AND COALESCE(endDate, 9999999999999))
        ORDER BY startTime ASC
    """)
    fun getActiveSchedules(today: Long): Flow<List<Schedule>>

    @Update
    suspend fun update(schedule: Schedule)

    @Delete
    suspend fun delete(schedule: Schedule)
}