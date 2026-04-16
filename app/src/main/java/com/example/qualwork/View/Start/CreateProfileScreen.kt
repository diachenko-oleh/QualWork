package com.example.qualwork.View.Start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.UserViewModel

@Composable
fun CreateProfileScreen(
    onProfileCreated: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    LaunchedEffect(viewModel.currentUser) {
        if (viewModel.currentUser != null) {
            onProfileCreated()
        }
    }
    QualWorkTheme {
        Scaffold(
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Ласкаво просимо",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text="Створіть профіль щоб розпочати",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = viewModel.nameInput,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Ваше ім'я") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text="Ви можете змінити ім'я у майбутньому",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = viewModel::createUser,
                    enabled = viewModel.isNameValid() && !viewModel.isLoading,
                    modifier = Modifier.fillMaxWidth(),

                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Створити профіль")
                    }
                }
            }
        }
    }
}