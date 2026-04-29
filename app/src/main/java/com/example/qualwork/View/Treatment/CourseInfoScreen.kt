package com.example.qualwork.View.Treatment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qualwork.Model.Entity.DayIntakeStat
import com.example.qualwork.ViewModel.CourseViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.qualwork.Model.Entity.DayStatus
import com.example.qualwork.Model.Entity.status
import com.example.qualwork.ViewModel.CourseInfoViewModel
import com.example.qualwork.ViewModel.formatDate
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseInfoScreen(
    courseId: Long,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onIntakeClick: (Long, Long) -> Unit,
    viewModel: CourseViewModel = hiltViewModel(),
    courseInfoViewModel: CourseInfoViewModel = hiltViewModel()
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val courseData = courses.find { it.schedules.any { s -> s.id == courseId } } ?: return
    val schedule = courseData.schedules.first()
    var showDeleteDialog by remember { mutableStateOf(false) }


    val nextDoseTimes = courseInfoViewModel.nextDoseTime
    val medAmounts by viewModel.medAmounts.collectAsStateWithLifecycle()
    val medAmount = medAmounts[schedule.id]
    val shouldShowRefill = medAmount != null && medAmount <= schedule.dosage



    LaunchedEffect(schedule.id) {
        viewModel.startWatchingActiveIntake(schedule.id)
        viewModel.loadCourse(schedule.id)
    }
    val activeIntakeTime = viewModel.activeIntakeTime
    val calendarStats by viewModel.getCalendarStats(
        scheduleId = schedule.id,
        startDateMillis = schedule.startDate,
        endDateMillis = schedule.endDate
    ).collectAsState(initial = emptyList())

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshActiveIntake(schedule.id)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Інформація про курс прийому") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(courseId) }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Редагувати")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Видалити"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
            )
        },
        floatingActionButton = {
            activeIntakeTime?.let { time ->
                val formattedTime = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                ExtendedFloatingActionButton(
                    onClick = {
                        val doseTimeMillis = LocalDate.now()
                            .atTime(time)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        onIntakeClick(schedule.id, doseTimeMillis)
                    },
                    icon = {
                        Icon(Icons.Rounded.Medication, contentDescription = null)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    text = { Text("Прийом за $formattedTime") }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        if (courseData == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val medication = courseData.medication
        val schedule = courseData.schedules.first()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            var showRefillDialog by remember { mutableStateOf(false) }
            // Препарат
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Препарат",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = medication.form.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))

                    InfoRow("Дозування:", "${schedule.dosage} ${medication.form.unit}")
                    Spacer(modifier = Modifier.height(10.dp))

                    medAmount?.let {
                        InfoRow("Залишилось:", "$it ${medication.form.unit}")
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    if (shouldShowRefill) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showRefillDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Поповнити")
                        }
                    }
                }
            }

            // Графік прийому
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Графік прийому",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()
                    viewModel.courseIntakeTimes.forEachIndexed { index, time ->
                        InfoRow(
                            "${index + 1}-й прийом",
                            time.substring(0, 5)
                        )
                    }
                    Text(
                        text = "Час наступного прийому:\n${schedule.id?.let { nextDoseTimes[it] }}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Тривалість
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Тривалість",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()
                    InfoRow("Початок", formatDate(schedule.startDate))
                    InfoRow(
                        "Кінець",
                        schedule.endDate?.let { formatDate(it) } ?: "Безстроково"
                    )
                }
            }

            Text(
                text = "Статистика прийому",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            LazyColumn {
                items(calendarStats) { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        week.forEach { day ->
                            DayStatCard(
                                stat = day,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(7 - week.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Видалити курс?") },
                    text = { Text("Курс лікування та всі пов'язані дані будуть видалені безповоротно.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteCourse(courseId)
                                showDeleteDialog = false
                                onBackClick()
                            }
                        ) {
                            Text(
                                text = "Видалити",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Скасувати")
                        }
                    }
                )
            }
            if (showRefillDialog) {
                var input by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showRefillDialog = false },
                    title = { Text("Поповнення препарату") },
                    text = {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            label = { Text("Кількість") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val value = input.toIntOrNull()
                                value?.let {
                                    if (it > 0) {
                                        viewModel.refillMedAmount(it, schedule)
                                        showRefillDialog = false
                                    }
                                }
                            }
                        ) {
                            Text("Зберегти")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRefillDialog = false }) {
                            Text("Скасувати")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DayStatCard(
    stat: DayIntakeStat,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val backgroundColor = when (stat.status) {
        DayStatus.ALL_TAKEN -> com.example.qualwork.View.theme.green
        DayStatus.ALL_MISSED -> com.example.qualwork.View.theme.red
        DayStatus.PARTIAL -> com.example.qualwork.View.theme.yellow
        DayStatus.FUTURE -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { showDialog = true },
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stat.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
    }

    if (showDialog && stat.intakes.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text=stat.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stat.intakes.forEach { intake ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Очікувався: ${intake.plannedTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")

                            intake.actualTime?.let { actual ->
                                Text(
                                    text = "Прийнято: ${actual.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                                )
                            }

                            Text(
                                text = if (intake.taken) "✓" else "✗",
                                color = if (intake.taken) com.example.qualwork.View.theme.green else com.example.qualwork.View.theme.red
                            )
                        }

                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Закрити")
                }
            }
        )
    }
}