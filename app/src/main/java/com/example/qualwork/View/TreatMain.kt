package com.example.qualwork.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qualwork.Model.Entity.MedicationWithSchedules
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.AddCourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatMainPage(
    onAddCourseClick: () -> Unit,
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
                            CourseCard(medicationWithSchedules = item)
                        }
                    }
                }
                Text("treatMain")
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
    modifier: Modifier = Modifier
) {
    val medication = medicationWithSchedules.medication
    val schedule = medicationWithSchedules.schedules.firstOrNull()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = medication.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = medication.form.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            schedule?.let {
                Text(
                    text = "ID курсу: ${it.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}