package com.example.qualwork.Model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.qualwork.Model.Entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {

    private val dataStore = context.dataStore

    val currentUserId: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }

    suspend fun saveCurrentUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun clearCurrentUserId() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }



    companion object {
        private val USER_ID_KEY = stringPreferencesKey("current_user_id")
        private val Context.dataStore by preferencesDataStore(name = "user_preferences")
    }
}