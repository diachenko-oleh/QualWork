package com.example.qualwork.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.qualwork.ViewModel.FilterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filterState: FilterState,
    onApply: (minPrice: Float, maxPrice: Float,maxPriceLimit:Float, onlyAvailable: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var minPrice by remember { mutableStateOf(filterState.minPrice) }
    var maxPrice by remember { mutableStateOf(filterState.maxPrice) }
    val maxPriceLimit = filterState.maxPriceLimit
    var onlyAvailable by remember { mutableStateOf(filterState.onlyAvailable) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Фільтри",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Фільтр за ціною
            Text(
                text = "Ціна: ${minPrice.toInt()} — ${maxPrice.toInt()} грн",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            RangeSlider(
                value = minPrice..maxPrice,
                onValueChange = { range ->
                    minPrice = range.start
                    maxPrice = range.endInclusive
                },
                valueRange = 0f..maxPriceLimit
            )

            // Фільтр за наявністю в аптеках
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onlyAvailable = !onlyAvailable },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Наявний в аптеках",
                    style = MaterialTheme.typography.titleSmall
                )
                Checkbox(
                    checked = onlyAvailable,
                    onCheckedChange = { onlyAvailable = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onApply(minPrice, maxPrice,maxPriceLimit, onlyAvailable) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Застосувати")
            }
        }
    }
}