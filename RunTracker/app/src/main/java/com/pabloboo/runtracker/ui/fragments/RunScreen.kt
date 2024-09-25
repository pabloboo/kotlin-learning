package com.pabloboo.runtracker.ui.fragments

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pabloboo.runtracker.R
import com.pabloboo.runtracker.db.Run
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RunScreen(
    filterOptions: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    runs: List<Run>,
    onRunClick: (Run) -> Unit,
    onAddRunClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRunClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Run",
                    tint = Color.Black
                )
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort by:",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DropdownMenu(
                        filterOptions = filterOptions,
                        selectedFilter = selectedFilter,
                        onFilterSelected = onFilterSelected
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(runs.size) { run ->
                        RunItem(run = runs[run], onClick = onRunClick)
                    }
                }
            }
        }
    )
}

@Composable
fun DropdownMenu(
    filterOptions: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Text(
            text = selectedFilter,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp)
        )
        androidx.compose.material3.DropdownMenu(
            content = {
                filterOptions.forEach { option ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            onFilterSelected(option)
                            expanded = false
                        }
                    )
                }
            },
            expanded = expanded,
            onDismissRequest = { expanded = false }
        )
    }
}

@Composable
fun RunItem(run: Run, onClick: (Run) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Image
        run.img?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        // Date and Time
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = dateFormatter.format(Date(run.timestamp))
        val time = timeFormatter.format(Date(run.timeInMillis))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = date, fontSize = 16.sp)
            Text(text = time, fontSize = 16.sp)
            Text(text = "${run.distanceInMeters / 1000f} km", fontSize = 16.sp)
            Text(text = "${run.avgSpeedInKMH} km/h", fontSize = 16.sp)
            Text(text = "${run.caloriesBurned} cal", fontSize = 16.sp)
        }
    }
}