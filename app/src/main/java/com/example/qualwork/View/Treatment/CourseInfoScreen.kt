package com.example.qualwork.View.Treatment

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qualwork.Model.Entity.DayIntakeStat
import com.example.qualwork.ViewModel.CourseViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.qualwork.Model.Entity.DayStatus
import com.example.qualwork.Model.Entity.IntakeLogStat
import com.example.qualwork.Model.Entity.status
import com.example.qualwork.ViewModel.CourseListViewModel
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
    readOnly: Boolean = false,
    viewModel: CourseViewModel = hiltViewModel(),
    courseListViewModel: CourseListViewModel = hiltViewModel()
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val patientCourseGroups = courseListViewModel.patientCourseGroups
    LaunchedEffect(Unit) {
        if (readOnly && courseListViewModel.patientCourseGroups.isEmpty()) {
            courseListViewModel.loadPatientCourses()
        }
    }

    val courseData = remember(courses, patientCourseGroups, courseId) {
        if (readOnly) {
            patientCourseGroups
                .flatMap { it.courses }
                .find { it.schedules.any { s -> s.id == courseId } }
        } else {
            courses.find { it.schedules.any { s -> s.id == courseId } }
        }
    }
    if (courseData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }



    val schedule = courseData.schedules.first()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val nextDoseTimes = if (readOnly) {
        patientCourseGroups
            .find { it.courses.any { c -> c.schedules.any { s -> s.id == courseId } } }
            ?.nextDoseTimes ?: emptyMap()
    } else {
        courseListViewModel.nextDoseTime
    }
    val medAmounts by viewModel.medAmounts.collectAsStateWithLifecycle()
    val medAmount = medAmounts[schedule.id]
    val shouldShowRefill = medAmount != null && medAmount <= schedule.dosage

    val patientId = remember(patientCourseGroups, courseId) {
        patientCourseGroups
            .find { it.courses.any { c -> c.schedules.any { s -> s.id == courseId } } }
            ?.patientId ?: ""
    }

    val calendarStats by if (readOnly && patientId.isNotEmpty()) {
        viewModel.getPatientCalendarStats(
            scheduleId = schedule.id,
            patientId = patientId,
            startDateMillis = schedule.startDate,
            endDateMillis = schedule.endDate
        )
    } else {
        viewModel.getCalendarStats(
            scheduleId = schedule.id,
            startDateMillis = schedule.startDate,
            endDateMillis = schedule.endDate
        )
    }.collectAsState(initial = emptyList())


    LaunchedEffect(schedule.id) {
        viewModel.startWatchingActiveIntake(schedule.id)
        viewModel.loadCourse(schedule.id)
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
                    if (!readOnly) {
                        IconButton(onClick = { onEditClick(courseId) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Редагувати")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Видалити")
                        }
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
            if (!readOnly) {
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
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
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
                        text = "Час наступного прийому:\n${
                            schedule.id.let {
                                nextDoseTimes[it]?.removePrefix("Наступний прийом: ")
                            }
                        }",
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

            //Статистика
            Text(
                text = "Статистика прийому",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
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
                item {
                    CourseStatsButton(
                        calendarStats = calendarStats,
                        startDate = schedule.startDate,
                        endDate = schedule.endDate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
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
        val takenCount = stat.intakes.count { it.taken }
        val totalCount = stat.intakes.size

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stat.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    // Підсумок прийомів
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            takenCount == totalCount -> com.example.qualwork.View.theme.green.copy(alpha = 0.15f)
                            takenCount == 0 -> com.example.qualwork.View.theme.red.copy(alpha = 0.15f)
                            else -> com.example.qualwork.View.theme.yellow.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = "Прийомів за день: $takenCount з $totalCount",
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                takenCount == totalCount -> com.example.qualwork.View.theme.green
                                takenCount == 0 -> com.example.qualwork.View.theme.red
                                else -> com.example.qualwork.View.theme.yellow
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stat.intakes.forEach { intake ->
                        IntakeLogRow(intake = intake)
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Закрити")
                    }
                }
            }
        )
    }
}

@Composable
private fun IntakeLogRow(intake: IntakeLogStat) {
    val isTaken = intake.taken
    val accentColor = if (isTaken) com.example.qualwork.View.theme.green
    else com.example.qualwork.View.theme.red

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.18f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = if (isTaken) "✓" else "✗",
                        fontSize = 16.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Заплановано",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = intake.plannedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (intake.actualTime != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Прийнято",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = intake.actualTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            } else if (!isTaken) {
                Text(
                    text = "Пропущено",
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CourseStatsButton(
    calendarStats: List<List<DayIntakeStat>>,
    startDate: Long,
    endDate: Long?,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Статистика курсу прийому")
    }

    if (showDialog) {
        CourseStatsDialog(
            calendarStats = calendarStats,
            startDate = startDate,
            endDate = endDate,
            onDismiss = { showDialog = false }
        )
    }
}
@Composable
fun CourseStatsDialog(
    calendarStats: List<List<DayIntakeStat>>,
    startDate: Long,
    endDate: Long?,
    onDismiss: () -> Unit
) {
    val stats = remember(calendarStats) { computeStats(calendarStats) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Статистика курсу прийому",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                //тривалість курсу
                CourseDateRange(startDate = startDate, endDate = endDate)

                HorizontalDivider()

                //коротка інфо
                StatsCardsRow(stats = stats)

                HorizontalDivider()

                //діаграма загальна
                Text(
                    text = "Розподіл прийомів",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DonutChart(
                        taken = stats.taken,
                        missed = stats.missed,
                        future = stats.future,
                        modifier = Modifier.size(140.dp)
                    )
                    DonutLegend(stats = stats)
                }

                HorizontalDivider()

                //діаграма по тижнях
                Text(
                    text = "По тижнях",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                WeeklyBarChart(
                    calendarStats = calendarStats,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )

                HorizontalDivider()

                // Смуга прогресу
                CourseProgressBar(stats = stats)

                // Кнопка закрити
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Закрити")
                }
            }
        }
    }
}

@Composable
private fun StatsCardsRow(stats: CourseStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            label = "Всього",
            value = stats.totalIntakeCount.toString(),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Прийнято",
            value = stats.taken.toString(),
            color = com.example.qualwork.View.theme.green,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Пропущено",
            value = stats.missed.toString(),
            color = com.example.qualwork.View.theme.red,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}



@Composable
private fun DonutChart(
    taken: Int,
    missed: Int,
    future: Int,
    modifier: Modifier = Modifier
) {
    val total = (taken + missed + future).coerceAtLeast(1).toFloat()
    val takenAngle = 360f * taken / total
    val missedAngle = 360f * missed / total
    val futureAngle = 360f * future / total

    val takenColor = com.example.qualwork.View.theme.green
    val missedColor = com.example.qualwork.View.theme.red
    val futureColor = com.example.qualwork.View.theme.lightGray

    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animated = true }
    val sweep by animateFloatAsState(
        targetValue = if (animated) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "donut"
    )

    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.18f
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(inset, inset)
        val style = Stroke(width = stroke, cap = StrokeCap.Butt)

        var startAngle = -90f

        listOf(
            takenAngle to takenColor,
            missedAngle to missedColor,
            futureAngle to futureColor
        ).forEach { (angle, color) ->
            val sweepA = angle * sweep
            if (sweepA > 0f) {
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepA,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style
                )
                startAngle += sweepA
            }
        }
    }
}

@Composable
private fun DonutLegend(stats: CourseStats) {
    val total = (stats.taken + stats.missed + stats.future).coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LegendItem(
            color = Color(0xFF4CAF50),
            label = "Прийнято",
            percent = stats.taken * 100 / total
        )
        LegendItem(
            color = Color(0xFFF44336),
            label = "Пропущено",
            percent = stats.missed * 100 / total
        )
        LegendItem(
            color = Color(0xFFBDBDBD),
            label = "Заплановано",
            percent = stats.future * 100 / total
        )
    }
}
@Composable
private fun LegendItem(color: Color, label: String, percent: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = "$label — $percent%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}



@Composable
private fun WeeklyBarChart(
    calendarStats: List<List<DayIntakeStat>>,
    modifier: Modifier = Modifier
) {
    val weeks: List<WeekBar> = remember(calendarStats) {
        calendarStats.mapIndexed { index, week ->
            val pastDays = week.filter { it.status != DayStatus.FUTURE }
            val allIntakes = pastDays.flatMap { it.intakes }
            val taken = allIntakes.count { it.taken }
            val missed = allIntakes.count { !it.taken }
            WeekBar(
                label = "Тиж.${index + 1}",
                taken = taken,
                missed = missed,
                total = allIntakes.size.coerceAtLeast(1)
            )
        }
    }

    if (weeks.isEmpty()) {
        Text(
            text = "Немає даних",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val takenColor = com.example.qualwork.View.theme.green
    val missedColor = com.example.qualwork.View.theme.red
    val maxVal = weeks.maxOf { it.taken + it.missed}.coerceAtLeast(1).toFloat()

    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animated = true }
    val progress by animateFloatAsState(
        targetValue = if (animated) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "bars"
    )

    Canvas(modifier = modifier) {
        val barAreaWidth = size.width
        val barAreaHeight = size.height - 28.dp.toPx()
        val groupWidth = barAreaWidth / weeks.size
        val barWidth = groupWidth * 0.55f
        val barOffset = (groupWidth - barWidth) / 2f

        weeks.forEachIndexed { i, week ->
            val x = i * groupWidth + barOffset
            val scaleH = barAreaHeight / maxVal

            var currentY = barAreaHeight


            val takenH = week.taken * scaleH * progress
            currentY -= takenH
            if (takenH > 0f) {
                drawRoundRect(
                    color = takenColor,
                    topLeft = Offset(x, currentY),
                    size = Size(barWidth, takenH),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }

            val missedH = week.missed * scaleH * progress
            currentY -= missedH
            if (missedH > 0f) {
                drawRoundRect(
                    color = missedColor,
                    topLeft = Offset(x, currentY),
                    size = Size(barWidth, missedH),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }

            drawContext.canvas.nativeCanvas.drawText(
                week.label,
                x + barWidth / 2f,
                barAreaHeight + 22.dp.toPx(),
                android.graphics.Paint().apply {
                    textSize = 11.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = android.graphics.Color.GRAY
                }
            )
        }
    }
}

private data class WeekBar(
    val label: String,
    val taken: Int,
    val missed: Int,
    val total: Int
)


@Composable
private fun CourseProgressBar(stats: CourseStats) {
    val pastTotal = (stats.taken + stats.missed).coerceAtLeast(1)
    val completionPercent = stats.taken * 100 / pastTotal

    val doneIntakes = stats.taken + stats.missed
    val coursePercent = doneIntakes * 100 / stats.totalIntakeCount.coerceAtLeast(1)

    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animated = true }
    val progress by animateFloatAsState(
        targetValue = if (animated) pastTotal.toFloat() else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Завершеність курсу",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$coursePercent%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = com.example.qualwork.View.theme.green
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = com.example.qualwork.View.theme.green,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = "Пройдено $completionPercent% курсу  •  ${stats.taken} з ${stats.totalIntakeCount} запланованих прийнято",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
private fun CourseDateRange(startDate: Long, endDate: Long?) {
    val fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val start = java.time.Instant.ofEpochMilli(startDate)
        .atZone(ZoneId.systemDefault()).toLocalDate()
    val end = endDate?.let {
        java.time.Instant.ofEpochMilli(it)
            .atZone(ZoneId.systemDefault()).toLocalDate()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = start.format(fmt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = " - ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = end?.format(fmt) ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
data class CourseStats(
    val totalIntakeCount: Int,
    val taken: Int,
    val missed: Int,
    val future: Int,
)

private fun computeStats(calendarStats: List<List<DayIntakeStat>>): CourseStats {
    val allDays = calendarStats.flatten()

    var taken = 0
    var missed = 0
    var partial = 0
    var future = 0

    allDays.forEach { day ->
        when (day.status) {
            DayStatus.ALL_TAKEN -> {
                taken += day.intakes.count { it.taken }
            }
            DayStatus.ALL_MISSED -> {
                missed += day.intakes.size
            }
            DayStatus.PARTIAL -> {
                taken += day.intakes.count { it.taken }
                missed += day.intakes.count { !it.taken }
                partial++
            }
            DayStatus.FUTURE -> {
                future += day.intakes.size.coerceAtLeast(1)
            }
        }
    }

    val total = taken + missed + future

    return CourseStats(
        totalIntakeCount = total,
        taken = taken,
        missed = missed,
        future = future
    )
}