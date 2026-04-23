package com.example.qualwork.Model.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.qualwork.Model.Entity.IntakeLog
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeLogDao {

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: IntakeLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<IntakeLog>): List<Long>


    // Update
    @Update
    suspend fun update(log: IntakeLog)


    // Delete
    @Delete
    suspend fun delete(log: IntakeLog)

    @Query("DELETE FROM intake_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM intake_logs WHERE scheduleId = :scheduleId")
    suspend fun deleteByScheduleId(scheduleId: Long)


    // Get by ID
    @Query("SELECT * FROM intake_logs WHERE id = :id")
    suspend fun getById(id: Long): IntakeLog?


    // Get all logs
    @Query("SELECT * FROM intake_logs ORDER BY intakeTime DESC")
    fun getAll(): Flow<List<IntakeLog>>

    @Query("SELECT * FROM intake_logs WHERE scheduleId = :scheduleId")
    fun getByScheduleId(scheduleId: Long): Flow<List<IntakeLog>>

    @Query("SELECT * FROM intake_logs WHERE scheduleId = :scheduleId AND doseTime = :doseTime LIMIT 1")
    suspend fun getByScheduleAndDoseTime(scheduleId: Long, doseTime: Long): IntakeLog?

    @Query("""
        SELECT * FROM intake_logs
        WHERE intakeTime BETWEEN :from AND :to
        ORDER BY intakeTime DESC
    """)
    fun getByTimeRange(from: Long, to: Long): Flow<List<IntakeLog>>

    @Query("""
        SELECT * FROM intake_logs
        WHERE scheduleId = :scheduleId AND taken = :taken
        ORDER BY intakeTime DESC
    """)
    fun getByTakenStatus(scheduleId: Long, taken: Boolean): Flow<List<IntakeLog>>


    // Count statistics
    @Query("""
        SELECT COUNT(*) FROM intake_logs
        WHERE scheduleId = :scheduleId AND taken = 1
    """)
    suspend fun countTaken(scheduleId: Long): Int

    @Query("""
        SELECT COUNT(*) FROM intake_logs
        WHERE scheduleId = :scheduleId AND taken = 0
    """)
    suspend fun countMissed(scheduleId: Long):Int

    @Query("SELECT * FROM intake_logs WHERE scheduleId = :scheduleId ORDER BY doseTime DESC LIMIT 1")
    suspend fun getLastLog(scheduleId: Long): IntakeLog?
}