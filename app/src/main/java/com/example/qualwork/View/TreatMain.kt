package com.example.qualwork.View

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.qualwork.View.theme.QualWorkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatMainPage(onAddCourseClick: () -> Unit){
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
                //verticalArrangement = Arrangement.Top,
                //horizontalAlignment = Alignment.Companion.CenterHorizontally,
            ){
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