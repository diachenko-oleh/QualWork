package com.example.qualwork.View.Treatment

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qualwork.Model.Notification.SupervisorNotificationHelper
import com.example.qualwork.Model.Relation.MedicationWithSchedules
import com.example.qualwork.Model.Repository.NetworkBannerState
import com.example.qualwork.Model.Repository.NetworkObserver
import com.example.qualwork.Model.Repository.stateContainer
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.CourseListViewModel
import com.example.qualwork.ViewModel.CourseViewModel
import com.example.qualwork.ViewModel.NetworkUtils
import com.example.qualwork.ViewModel.UserViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatMainPage(
    onAddCourseClick: () -> Unit,
    onCourseClick: (Long) -> Unit,
    viewModel: CourseViewModel = hiltViewModel(),
    courseListViewModel: CourseListViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val nextDoseTimes = courseListViewModel.nextDoseTime
    val nextDoseTimesRaw = courseListViewModel.nextDoseTimeRaw

    val sortedCourses = remember(courses, nextDoseTimesRaw) {
        courses.sortedWith(compareBy { item ->
            val scheduleId = item.schedules.firstOrNull()?.id ?: return@compareBy LocalTime.MAX
            val (time, isTomorrow) = nextDoseTimesRaw[scheduleId] ?: return@compareBy LocalTime.MAX
            if (isTomorrow) {
                LocalDate.now().plusDays(1).atTime(time)
            } else {
                LocalDate.now().atTime(time)
            }
        })
    }

    val patientCourseGroups = courseListViewModel.patientCourseGroups

    val context = LocalContext.current
    val networkObserver = remember { NetworkObserver(context) }
    val isOnline by networkObserver.isOnline.collectAsStateWithLifecycle(
        initialValue = NetworkUtils.isOnline(context)
    )
    var bannerState by remember { mutableStateOf(
        if (NetworkUtils.isOnline(context)) NetworkBannerState.HIDDEN
        else NetworkBannerState.OFFLINE
    )}

    LaunchedEffect(isOnline) {
        if (isOnline && bannerState == NetworkBannerState.OFFLINE) {
            bannerState = NetworkBannerState.SYNCING
            userViewModel.currentUser?.id?.let { userId ->
                userViewModel.syncOnStartup(context, userId)
            }
            delay(3000)
            bannerState = NetworkBannerState.HIDDEN
        } else if (!isOnline) {
            bannerState = NetworkBannerState.OFFLINE
        }
    }
    LaunchedEffect(Unit) {
        courseListViewModel.loadPatientCourses()
    }
    LaunchedEffect(patientCourseGroups) {
        val patientIds = patientCourseGroups.map { it.patientId }
        courseListViewModel.startObservingMissedNotifications(patientIds) {
                patientName, medicationName, time ->
            SupervisorNotificationHelper.showMissedNotification(
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
                    actions = {
                        if (courseListViewModel.isLoadingPatientCourses) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            IconButton(
                                onClick = { courseListViewModel.loadPatientCourses() }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Оновити",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    },
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
                OfflineBanner(state = bannerState)

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sortedCourses) {item ->
                            val scheduleId = item.schedules.firstOrNull()?.id
                           CourseCard(
                               medicationWithSchedules = item,
                               nextDoseTime = scheduleId?.let { nextDoseTimes[it] },
                               onClick = { onCourseClick(item.schedules.first().id) }
                           )
                        }
                        items(patientCourseGroups) { group ->
                            CollapsibleCourseSection(
                                title = "Курси прийому: ${group.patientName}",
                                courses = group.courses,
                                nextDoseTimes = group.nextDoseTimes
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
    nextDoseTime: String?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val medication = medicationWithSchedules.medication
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
    isLoading: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
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
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${courses.size} курс${
                                when {
                                    courses.size % 10 == 1 && courses.size % 100 != 11 -> ""
                                    courses.size % 10 in 2..4 && courses.size % 100 !in 12..14 -> "и"
                                    else -> "ів"
                                }
                            }",
                            style = MaterialTheme.typography.bodyMedium,
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
                            style = MaterialTheme.typography.bodyLarge,
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
                                    onClick = null
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun OfflineBanner(state: NetworkBannerState) {
    val isVisible = state != NetworkBannerState.HIDDEN

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        val (backgroundColor, contentColor, icon, text) = when (state) {
            NetworkBannerState.OFFLINE -> stateContainer(
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
                Icons.Rounded.Warning,
                "Відсутній зв'язок з мережею — пошук ліків неможливий, а дані можуть бути застарілими"
            )
            NetworkBannerState.SYNCING -> stateContainer(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
                Icons.Rounded.Refresh,
                "Зв'язок відновлено. Виконується синхронізація..."
            )
            NetworkBannerState.HIDDEN -> return@AnimatedVisibility
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }
        }
    }
}
