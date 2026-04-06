package com.example.qualwork.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import coil.compose.AsyncImage
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import com.example.qualwork.Data.Model.Medicine
import com.example.qualwork.View.theme.QualWorkTheme
import com.example.qualwork.ViewModel.MedicineSearchState
import com.example.qualwork.ViewModel.MyViewModel
@ExperimentalMaterial3Api
@Composable
fun SearchBarScreen(viewModel: MyViewModel,openMedInfo: (Medicine) -> Unit,onBack: () -> Unit){
    QualWorkTheme {
        val query by viewModel.searchQuery.collectAsState()
        val searchState by viewModel.searchState.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            Modifier
                                .padding(end = 10.dp)
                                .background(MaterialTheme.colorScheme.primary),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = query,
                                onValueChange = { viewModel.updateQuery(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp),
                                textStyle = TextStyle(fontSize = 20.sp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = { viewModel.search(query) }
                                ),
                                decorationBox = { innerTextField ->
                                    Box(
                                        contentAlignment = Alignment.CenterStart,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        if (query.isEmpty()) {
                                            Text(
                                                "Введіть назву...",
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontSize = 20.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                },

                            )

                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    },

                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                when (val state = searchState) {
                    is MedicineSearchState.Idle -> {}
                    is MedicineSearchState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is MedicineSearchState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is MedicineSearchState.Success -> {
                        val exactMatches = state.medicines.filter { it.isExact }
                        val similarMatches = state.medicines.filter { !it.isExact }

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                            items(exactMatches) { medicine ->
                                MedicineCard(medicine, onClick = { openMedInfo(medicine) })
                            }

                            if (similarMatches.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Нічого не знайдено",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                    Text(
                                        text = "Можливо ви шукали це:",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                    HorizontalDivider()
                                }
                                items(similarMatches) { medicine ->
                                    MedicineCard(medicine, onClick = { openMedInfo(medicine) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun MedicineCard(
    medicine: Medicine,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(
                width = 1.dp,
                color = com.example.qualwork.View.theme.lightGray
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = medicine.imageUrl,
                contentDescription = medicine.name,
                modifier = Modifier
                    .size(150.dp)
                    .padding(end = 12.dp),
                contentScale = ContentScale.Fit
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = medicine.manufacturer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = medicine.minPrice,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}