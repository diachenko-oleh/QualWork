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

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getById(id: Long): Schedule?

    @Query("""
        SELECT * FROM schedules
        WHERE userId = :userId
    """)
    suspend fun getByUser(userId: String): List<Schedule>

    @Update
    suspend fun update(schedule: Schedule)

    @Query("UPDATE schedules SET medAmount = :amount WHERE id = :scheduleId")
    suspend fun updateMedAmount(scheduleId: Long, amount: Int)

    @Delete
    suspend fun delete(schedule: Schedule)


}