package com.example.qualwork.View.Treatment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qualwork.Model.Entity.MedicationForm
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.CourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCourse(
    courseId: Long? = null,
    onBackClick: () -> Unit,
    onCourseAdded: () -> Unit,
    viewModel: CourseViewModel = hiltViewModel()
){
    var currentStep by remember { mutableIntStateOf(1) }
    LaunchedEffect(viewModel.savedSuccessfully) {
        if (viewModel.savedSuccessfully) onCourseAdded()
    }
    LaunchedEffect(courseId) {
        courseId?.let { viewModel.loadCourse(it) }
    }
    QualWorkTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Новий курс лікування") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                )
            }
        ) {padding->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StepIndicator(currentStep = currentStep, totalSteps = 3)
                when (currentStep) {
                    1 -> Step1Content(viewModel = viewModel)
                    2 -> Step2Content(viewModel = viewModel)
                    3 -> Step3Content(viewModel = viewModel)
                }
                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Назад")
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < 3) {
                                currentStep++
                            } else {
                                viewModel.saveCourse()
                            }
                        },
                        enabled = when (currentStep) {
                            1 -> viewModel.isStep1Valid()
                            2 -> viewModel.isStep2Valid()
                            3 -> viewModel.isStep3Valid() && !viewModel.isSaving
                            else -> false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        if (viewModel.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (currentStep < 3) "Далі" else "Зберегти")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val step = index + 1
            val isActive = step == currentStep
            val isDone = step < currentStep

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            isDone -> MaterialTheme.colorScheme.primary
                            isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            )
        }
    }

    Text(
        text = when (currentStep) {
            1 -> "Крок 1: Препарат"
            2 -> "Крок 2: Графік прийому"
            3 -> "Крок 3: Тривалість"
            else -> ""
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1Content(viewModel: CourseViewModel){
    val forms = MedicationForm.entries
    var formDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = viewModel.medicationName,
            onValueChange = viewModel::onNameChange,
            label = {Text("Назва препарату")},
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = formDropdownExpanded,
            onExpandedChange = { formDropdownExpanded =it}
        ) {
            OutlinedTextField(
                value = viewModel.medicationForm.displayName,
                onValueChange = {},
                readOnly = true,
                label = {Text("Форма препарату")},
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = formDropdownExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = formDropdownExpanded,
                onDismissRequest = {formDropdownExpanded = false},
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                forms.forEach {form->
                    DropdownMenuItem(
                        text = {Text(form.displayName)},
                        onClick = {
                            viewModel.onFormChange(form)
                            formDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step2Content(viewModel: CourseViewModel) {
    val intervals = listOf(4, 6, 8, 12, 24, 48, 72)
    var intervalDropdownExpanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Дозування: ${viewModel.dosage} ${viewModel.medicationForm.unit}",
                style = MaterialTheme.typography.titleMedium
            )
            Row {
                IconButton(
                    onClick = { if (viewModel.dosage > 1) viewModel.onDosageChange(viewModel.dosage - 1) }
                ) {
                    Icon(Icons.Rounded.Remove, contentDescription = "Менше")
                }
                IconButton(
                    onClick = { viewModel.onDosageChange(viewModel.dosage + 1) }
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Більше")
                }
            }
        }

        HorizontalDivider()

        ExposedDropdownMenuBox(
            expanded = intervalDropdownExpanded,
            onExpandedChange = { intervalDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = intervalLabel(viewModel.intervalHours),
                onValueChange = {},
                readOnly = true,
                label = { Text("Частота прийому") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalDropdownExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = intervalDropdownExpanded,
                onDismissRequest = { intervalDropdownExpanded = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                intervals.forEach { hours ->
                    DropdownMenuItem(
                        text = { Text(intervalLabel(hours)) },
                        onClick = {
                            viewModel.onIntervalChange(hours)
                            intervalDropdownExpanded = false
                        }
                    )
                }
            }
        }

        HorizontalDivider()

        OutlinedTextField(
            value = viewModel.startTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("Час першого прийому") },
            trailingIcon = {
                IconButton(onClick = { showTimePicker = true }) {
                    Icon(Icons.Rounded.Schedule, contentDescription = "Обрати час")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = viewModel.startTime,
            onTimeSelected = { time ->
                viewModel.onStartTimeChange(time)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun Step3Content(viewModel: CourseViewModel) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var isIndefinite by remember { mutableStateOf(viewModel.endDate == null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        OutlinedTextField(
            value = formatDate(viewModel.startDate),
            onValueChange = {},
            readOnly = true,
            label = { Text("Дата початку") },
            trailingIcon = {
                IconButton(onClick = { showStartDatePicker = true }) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = "Обрати дату")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Безстроково",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = isIndefinite,
                onCheckedChange = { checked ->
                    isIndefinite = checked
                    viewModel.onEndDateChange(if (checked) null else System.currentTimeMillis())
                }
            )
        }

        if (!isIndefinite) {
            OutlinedTextField(
                value = viewModel.endDate?.let { formatDate(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Дата закінчення") },
                trailingIcon = {
                    IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = "Обрати дату")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            initialDate = viewModel.startDate,
            onDateSelected = { date ->
                viewModel.onStartDateChange(date)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            initialDate = viewModel.endDate ?: System.currentTimeMillis(),
            onDateSelected = { date ->
                viewModel.onEndDateChange(date)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                        selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Скасувати", color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    }) {
                        Text("Підтвердити", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val (initHour, initMinute) = initialTime.split(":").map { it.toInt() }
    val timePickerState = rememberTimePickerState(
        initialHour = initHour,
        initialMinute = initMinute,
        is24Hour = true
    )
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 24.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Скасувати")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = {
                        val hour = timePickerState.hour.toString().padStart(2, '0')
                        val minute = timePickerState.minute.toString().padStart(2, '0')
                        onTimeSelected("$hour:$minute")
                    }) {
                        Text("Підтвердити")
                    }
                }
            }
        }
    }
}
