package com.pabloboo.runtracker.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.mikephil.charting.charts.LineChart
import com.pabloboo.runtracker.ui.viewmodels.StatisticsViewModel
import com.pabloboo.runtracker.utils.TrackingUtility
import kotlin.math.round

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel
    //chartData: LineChart
) {
    var totalTime by remember { mutableStateOf("00:00:00") }
    viewModel.totalTimeRun.observe(LocalLifecycleOwner.current, Observer {
        it?.let {
            val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
            totalTime = totalTimeRun
        }
    })

    var totalDistance by remember { mutableStateOf("0km") }
    viewModel.totalDistance.observe(LocalLifecycleOwner.current, Observer {
        it?.let {
            val km = it / 1000f
            totalDistance = "${round(km * 10f) / 10f}km"
        }
    })

    var totalCalories by remember { mutableStateOf("0kcal") }
    viewModel.totalCaloriesBurned.observe(LocalLifecycleOwner.current, Observer {
        it?.let {
            totalCalories = "${it}kcal"
        }
    })

    var averageSpeed by remember { mutableStateOf("0km/h") }
    viewModel.totalAvgSpeed.observe(LocalLifecycleOwner.current, Observer {
        it?.let {
            val speed = round(it * 10f) / 10f
            averageSpeed = "${speed}km/h"
        }
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Total Distance Section
        StatSection(
            title = "Total Distance",
            value = totalDistance
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total Time Section
        StatSection(
            title = "Total Time",
            value = totalTime
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total Calories Section
        StatSection(
            title = "Total Calories Burned",
            value = totalCalories
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Average Speed Section
        StatSection(
            title = "Average Speed",
            value = averageSpeed
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Line Chart
        //LineChartComposable(chartData)
    }
}

@Composable
fun StatSection(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun LineChartComposable(chartData: LineChart) {
    // Handle the actual chart rendering.
}