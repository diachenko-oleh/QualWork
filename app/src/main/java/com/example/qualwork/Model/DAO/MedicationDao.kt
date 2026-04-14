package com.example.qualwork.Model.DAO
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.qualwork.Model.Entity.Medication
import com.example.qualwork.Model.Entity.MedicationWithSchedules
import kotlinx.coroutines.flow.Flow


@Dao
interface MedicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication): Long

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAll(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getById(id: Long): Medication?

    @Delete
    suspend fun delete(medication: Medication)

    @Transaction
    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllWithSchedules(): Flow<List<MedicationWithSchedules>>
}