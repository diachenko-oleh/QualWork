package com.example.qualwork.Model.Repository

import com.example.qualwork.Model.DAO.UserDao
import com.example.qualwork.Model.Entity.User
import com.example.qualwork.Model.UserPreferences
import kotlinx.coroutines.flow.first
import java.util.UUID

class UserRepository(
    private val userDao: UserDao
) {
    suspend fun createUser(name: String): User {
        val user = User(
            id = IdGenerator.generateId(),
            name = name,
            code = generateUniqueCode()
        )
        userDao.insert(user)
        return user
    }

    suspend fun getById(id: String): User? = userDao.getById(id)
    suspend fun getByCode(code: String): User? = userDao.getByCode(code)
    fun getAllUsers(): List<User> {
        return userDao.getAll()
    }
    private suspend fun generateUniqueCode(): String {
        var code = CodeGenerator.generate()
        while (userDao.getByCode(code) != null) {
            code = CodeGenerator.generate()
        }
        return code
    }
    suspend fun updateName(userId: String, newName: String): User? {
        val user = userDao.getById(userId) ?: return null
        val updatedUser = user.copy(name = newName)
        userDao.update(updatedUser)
        return updatedUser
    }
}

object IdGenerator {
    fun generateId(): String = UUID.randomUUID().toString()
}
object CodeGenerator {
    fun generate(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}