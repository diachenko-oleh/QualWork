package com.example.qualwork.Model.Repository

import com.example.qualwork.Model.DAO.UserDao
import com.example.qualwork.Model.Entity.User
import java.util.UUID

class UserRepository(
    private val userDao: UserDao,
    private val firestoreRepository: FirestoreRepository
) {
    suspend fun createUser(name: String): User {
        val user = User(
            id = IdGenerator.generateId(),
            name = name,
            code = generateUniqueCode()
        )
        userDao.insert(user)
        firestoreRepository.syncUser(user)
        return user
    }

    suspend fun findUserByCode(code: String): User? =
        firestoreRepository.findUserByCode(code)

    suspend fun connectToPatient(supervisorId: String, code: String): Boolean =
        firestoreRepository.connectToPatient(supervisorId, code)

    suspend fun getPatientIds(supervisorId: String): List<String> =
        firestoreRepository.getPatientIds(supervisorId)


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

    suspend fun getPatients(supervisorId: String): List<User> =
        firestoreRepository.getPatients(supervisorId)

    suspend fun getSupervisors(patientId: String): List<User> =
        firestoreRepository.getSupervisors(patientId)

    suspend fun removeLink(userId1: String, userId2: String): Boolean =
        firestoreRepository.removeLink(userId1, userId2)
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