package com.example.qualwork.Model.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.qualwork.Model.Entity.Connection
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(connection: Connection): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(connections: List<Connection>): List<Long>
    @Delete
    suspend fun delete(connection: Connection)

    @Query("DELETE FROM connections WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM connections WHERE userId = :userId AND supervisorId = :supervisorId")
    suspend fun deleteByUserAndSupervisor(userId: Long, supervisorId: Long)

    @Query("SELECT * FROM connections WHERE id = :id")
    suspend fun getById(id: Long): Connection?

    @Query("SELECT * FROM connections")
    fun getAll(): Flow<List<Connection>>

    @Query("SELECT * FROM connections WHERE userId = :userId")
    fun getSupervisorsForUser(userId: Long): Flow<List<Connection>>

    @Query("SELECT * FROM connections WHERE supervisorId = :supervisorId")
    fun getUsersForSupervisor(supervisorId: Long): Flow<List<Connection>>

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM connections 
            WHERE userId = :userId AND supervisorId = :supervisorId
        )
    """)
    suspend fun exists(userId: Long, supervisorId: Long): Boolean

    @Query("SELECT COUNT(*) FROM connections WHERE userId = :userId")
    suspend fun countForUser(userId: Long): Int
}