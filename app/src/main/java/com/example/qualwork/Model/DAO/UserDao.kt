package com.example.qualwork.Model.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.qualwork.Model.Entity.User
import kotlinx.coroutines.flow.Flow
@Dao
interface UserDao {

    // Insert / Update
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>)

    @Update
    suspend fun update(user: User)

    // Delete
    @Delete
    suspend fun delete(user: User)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: String)


    // Get by ID
    @Query("SELECT * FROM users WHERE id = :id")
        suspend fun getById(id: String): User?


    // Get all users
    @Query("SELECT * FROM users")
    fun getAll(): List<User>


    // Find by code (наприклад, для логіну/реєстрації або пошуку)
    @Query("SELECT * FROM users WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): User?


    // Search by name (для UI пошуку)
    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%'")
    fun searchByName(query: String): Flow<List<User>>


    // Check existence
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("""
    SELECT * FROM users
    WHERE id IN (
        SELECT userId FROM connections
        WHERE supervisorId = :supervisorId
    )
""")
    suspend fun getUsersForSupervisor(supervisorId: String): List<User>
}