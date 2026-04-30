package com.example.qualwork.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualwork.Model.Entity.User
import com.example.qualwork.Model.Repository.UserRepository
import com.example.qualwork.Model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var nameInput by mutableStateOf("")
        private set

    fun onNameChange(value: String) { nameInput = value }
    fun isNameValid() = nameInput.isNotBlank()
    var usersInDb by mutableStateOf<List<User>>(emptyList())
        private set

    fun loadUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            usersInDb = userRepository.getAllUsers()
        }
    }
    fun loadCurrentUser() {
        viewModelScope.launch {
            val id = userPreferences.currentUserId.first()
            currentUser = id?.let { userRepository.getById(it) }
        }
    }

    suspend fun checkUser(): SplashDestination {
        val savedId = userPreferences.currentUserId.first()
        val user = savedId?.let { userRepository.getById(it) }

        return if (user != null) {
            SplashDestination.Home
        } else {
            SplashDestination.CreateProfile
        }
    }
    fun createUser() {
        if (!isNameValid()) return
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val user = userRepository.createUser(name = nameInput.trim())
                userPreferences.saveCurrentUserId(user.id)
                currentUser = user
            } catch (e: Exception) {
                error = "Помилка створення користувача"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            currentUser?.let { user ->
                currentUser = userRepository.updateName(user.id, newName.trim())
            }
        }
    }
    var isConnecting by mutableStateOf(false)
        private set
    var connectError by mutableStateOf<String?>(null)
        private set
    var connectSuccess by mutableStateOf(false)
        private set

    fun connectToPatient(code: String) {
        viewModelScope.launch {
            isConnecting = true
            connectError = null
            connectSuccess = false

            val currentId = currentUser?.id ?: return@launch

            if (code == currentUser?.code) {
                connectError = "Не можна підключити самого себе"
                isConnecting = false
                return@launch
            }

            val success = userRepository.connectToPatient(currentId, code)

            if (success) {
                connectSuccess = true
            } else {
                connectError = "Код не знайдено або вже використано"
            }

            isConnecting = false
        }
    }

    var patients by mutableStateOf<List<User>>(emptyList())
        private set
    var supervisors by mutableStateOf<List<User>>(emptyList())
        private set
    var isLoadingLinks by mutableStateOf(false)
        private set

    fun loadLinks() {
        viewModelScope.launch {
            isLoadingLinks = true
            val currentId = currentUser?.id ?: return@launch
            patients = userRepository.getPatients(currentId)
            supervisors = userRepository.getSupervisors(currentId)
            isLoadingLinks = false
        }
    }

    fun removeLink(otherUserId: String) {
        viewModelScope.launch {
            val currentId = currentUser?.id ?: return@launch
            val success = userRepository.removeLink(currentId, otherUserId)
            if (success) loadLinks()
        }
    }
}
sealed class SplashDestination {
    object Home : SplashDestination()
    object CreateProfile : SplashDestination()
}