package com.example.qualwork.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qualwork.Model.Entity.MedicationForm
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.AddCourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCourse(
    onCourseAdded: () -> Unit,
    viewModel: AddCourseViewModel = hiltViewModel()
){
    val forms = MedicationForm.entries
    var formDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.savedSuccessfully) {
        if (viewModel.savedSuccessfully) onCourseAdded()
    }
    QualWorkTheme {
        Scaffold(

        ) {padding->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Новий курс лікування",
                    style = MaterialTheme.typography.headlineSmall
                )

                // Назва препарату
                OutlinedTextField(
                    value = viewModel.medicationName,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Назва препарату") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Форма препарату
                ExposedDropdownMenuBox(
                    expanded = formDropdownExpanded,
                    onExpandedChange = { formDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = viewModel.medicationForm.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Форма препарату") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = formDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = formDropdownExpanded,
                        onDismissRequest = { formDropdownExpanded = false }
                    ) {
                        forms.forEach { form ->
                            DropdownMenuItem(
                                text = { Text(form.displayName) },
                                onClick = {
                                    viewModel.onFormChange(form)
                                    formDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = viewModel::saveCourse,
                    enabled = viewModel.isStep1Valid() && !viewModel.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (viewModel.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Зберегти")
                    }
                }
            }
        }
    }
}
