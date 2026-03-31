package com.example.qualwork.ViewModel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

class MyViewModel(application: Application): AndroidViewModel(application) {
     val words = listOf(
         "корова",
         "корабль",
         "корень",
         "кот",
         "компьютер",
         "книга",
         "корова1",
         "корабль1",
         "корень1",
         "кот1",
         "компьютер1",
         "книга1",

    )

    private val _searchText = mutableStateOf("результат")
    val searchText: State<String> = _searchText
    fun updateText(newText: String) {
        _searchText.value = newText
    }
}