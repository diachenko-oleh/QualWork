package com.example.qualwork.View.Treatment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qualwork.Model.Notification.NotificationScheduler
import com.example.qualwork.Model.Relation.MedicationWithSchedules
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.AddCourseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatMainPage(
    onAddCourseClick: () -> Unit,
    onCourseClick: (Long) -> Unit,
    viewModel: AddCourseViewModel = hiltViewModel()){
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    QualWorkTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Перегляд курсів лікування") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                )
            },
            floatingActionButton = {
                AddCourseFab(onClick = onAddCourseClick)
            },
            floatingActionButtonPosition = FabPosition.End
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
            ){
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(courses) { item ->
                           CourseCard(
                               medicationWithSchedules = item,
                               onClick = { onCourseClick(item.schedules.first().id) }
                           )
                        }
                        item {
                            NotificationTestCard(
                                scheduler = viewModel.getScheduler()
                            )
                        }
                    }
                }
            }

        }
    }
}
@Composable
fun AddCourseFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick=onClick,
        modifier = Modifier.size(80.dp),
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Додати курс лікування",
            modifier = Modifier.size(35.dp)
        )
    }
}
@Composable
fun CourseCard(
    medicationWithSchedules: MedicationWithSchedules,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val medication = medicationWithSchedules.medication
    val schedule = medicationWithSchedules.schedules.firstOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    )  {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Назва та форма
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = medication.form.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

           HorizontalDivider()

//            schedule?.let { s ->
//                // Дозування та інтервал
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    LabeledValue(
//                        label = "Дозування",
//                        value = "${s.dosage} ${medication.form.unit}"
//                    )
//                    LabeledValue(
//                        label = "Інтервал",
//                        value = intervalLabel(s.intervalHours)
//                    )
//                    LabeledValue(
//                        label = "Час прийому",
//                        value = s.startTime
//                    )
//                }
//
//                HorizontalDivider()
//
//                // Дати
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    LabeledValue(
//                        label = "Початок",
//                        value = formatDate(s.startDate)
//                    )
//                    LabeledValue(
//                        label = "Кінець",
//                        value = s.endDate?.let { formatDate(it) } ?: "Безстроково"
//                    )
//                    LabeledValue(
//                        label = "ID курсу",
//                        value = "${s.id}"
//                    )
//                }
//            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
@Composable
fun NotificationTestCard(
    scheduler: NotificationScheduler,
    modifier: Modifier = Modifier
) {
    var delayMinutes by remember { mutableIntStateOf(1) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Тест сповіщень",
                style = MaterialTheme.typography.titleMedium
            )

            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Затримка: $delayMinutes хв",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row {
                    IconButton(onClick = { if (delayMinutes > 1) delayMinutes-- }) {
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Менше")
                    }
                    IconButton(onClick = { if (delayMinutes < 60) delayMinutes++ }) {
                        Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Більше")
                    }
                }
            }

            // Кнопка із затримкою
            OutlinedButton(
                onClick = {
                    scheduler.scheduleDelayed(
                        delayMinutes = delayMinutes,
                        medicationName = "Тестовий препарат",
                        dosage = 1,
                        unit = "таблетка"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Відправити через $delayMinutes хв")
            }
        }
    }
}

private fun getCurrentTime(): String {
    val calendar = Calendar.getInstance()
    val hours = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
    val minutes = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
    return "$hours:$minutes"
}