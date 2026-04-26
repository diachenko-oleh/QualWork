package com.example.qualwork.View.Treatment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    viewModel: CourseViewModel = hiltViewModel()
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val courseData = courses.find { it.schedules.any { s -> s.id == courseId } }
        ?: return
    val schedule = courseData.schedules.first()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val stats by viewModel
        .getStats(courseId)
        .collectAsState(initial = emptyList())

    LaunchedEffect(schedule.id) {
        viewModel.startWatchingActiveIntake(schedule.id)
    }
    val activeIntakeTime = viewModel.activeIntakeTime

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
                title = { Text("Інформація про курс") },
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
        }
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
                    Text(
                        text = medication.form.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    InfoRow("Дозування", "${schedule.dosage} ${medication.form.unit}")
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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Час наступного прийому",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                }
            }
            Text(
                text = "Статистика прийому",
                style = MaterialTheme.typography.titleLarge
            )

            LazyRow {
                items(stats) { day ->
                    DayStatCard(day)
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


            activeIntakeTime?.let { time ->
                val formattedTime = time.format(DateTimeFormatter.ofPattern("HH:mm"))

                Button(
                    onClick = {
                        val doseTimeMillis = LocalDate.now()
                            .atTime(time)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        onIntakeClick(schedule.id, doseTimeMillis)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Записати прийом за $formattedTime")
                }
            } ?: run {
                LaunchedEffect(Unit) {
                    viewModel.markExpiredAsSkipped(schedule.id)
                }
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
fun DayStatCard(stat: DayIntakeStat) {

    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = stat.date,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Прийнято: ${stat.taken}",
                color = Color.Green
            )

            Text(
                text = "Пропущено: ${stat.missed}",
                color = Color.Red
            )
        }
    }
}