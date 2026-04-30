package com.example.qualwork.View.Treatment

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qualwork.Model.Notification.CaregiverNotificationHelper
import com.example.qualwork.Model.Notification.NotificationScheduler
import com.example.qualwork.Model.Relation.MedicationWithSchedules
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.CourseListViewModel
import com.example.qualwork.ViewModel.CourseViewModel

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatMainPage(
    onAddCourseClick: () -> Unit,
    onCourseClick: (Long) -> Unit,
    viewModel: CourseViewModel = hiltViewModel(),
    courseListViewModel: CourseListViewModel = hiltViewModel(),
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val nextDoseTimes = courseListViewModel.nextDoseTime
    val patientCourseGroups = courseListViewModel.patientCourseGroups
    LaunchedEffect(Unit) {
        courseListViewModel.loadPatientCourses()
    }
    val context = LocalContext.current
    LaunchedEffect(patientCourseGroups) {
        val patientIds = patientCourseGroups.map { it.patientId }
        courseListViewModel.startObservingMissedNotifications(patientIds) {
                patientName, medicationName, time ->
            CaregiverNotificationHelper.showMissedNotification(
                context = context,
                patientName = patientName,
                medicationName = medicationName,
                time = time
            )
        }
    }

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
                            val scheduleId = item.schedules.firstOrNull()?.id
                           CourseCard(
                               medicationWithSchedules = item,
                               nextDoseTime = scheduleId?.let { nextDoseTimes[it] },
                               onClick = { onCourseClick(item.schedules.first().id) }
                           )
                        }
//                        if (courseInfoViewModel.isLoadingPatientCourses) {
//                            item {
//                                Box(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    CircularProgressIndicator()
//                                }
//                            }
//                        }else {
                            items(patientCourseGroups) { group ->
                                CollapsibleCourseSection(
                                    title = "Курси лікування: ${group.patientName}",
                                    courses = group.courses,
                                    nextDoseTimes = group.nextDoseTimes,
                                    onCourseClick = onCourseClick
                                )
                            }
                        //}
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
    nextDoseTime: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val medication = medicationWithSchedules.medication


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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = nextDoseTime?.let { "Наступний прийом: $it" } ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CollapsibleCourseSection(
    title: String,
    courses: List<MedicationWithSchedules>,
    nextDoseTimes: Map<Long, String?>,
    isLoading: Boolean = false,
    onCourseClick: (Long) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            // Заголовок секції
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${courses.size} курс${
                                when {
                                    courses.size % 10 == 1 && courses.size % 100 != 11 -> ""
                                    courses.size % 10 in 2..4 && courses.size % 100 !in 12..14 -> "и"
                                    else -> "ів"
                                }
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded)
                        Icons.Rounded.KeyboardArrowUp
                    else
                        Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Згорнути" else "Розгорнути"
                )
            }

            // Вміст секції
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider()
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (courses.isEmpty()) {
                        Text(
                            text = "Немає активних курсів",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        courses.forEach { item ->
                            val scheduleId = item.schedules.firstOrNull()?.id
                            Box(modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 6.dp
                            )) {
                                CourseCard(
                                    medicationWithSchedules = item,
                                    nextDoseTime = scheduleId?.let { nextDoseTimes[it] },
                                    onClick = { onCourseClick(item.schedules.first().id) }
                                )
                            }
                        }
                    }
                }
            }
        }
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
    )
    {
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

            /*OutlinedButton(
                onClick = {
                    scheduler.scheduleDelayed(
                        delayMinutes = delayMinutes,
                        medicationName = "Тестовий препарат",
                        dosage = 1,
                        unit = "таблетка",
                        scheduleId = 9999L
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Відправити через $delayMinutes хв")
            }*/
        }
    }
}