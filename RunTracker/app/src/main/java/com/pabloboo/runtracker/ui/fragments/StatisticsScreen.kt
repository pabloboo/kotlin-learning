package com.pabloboo.runtracker.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.mikephil.charting.charts.LineChart

@Composable
fun StatisticsScreen(
    totalDistance: String,
    totalTime: String,
    totalCalories: String,
    averageSpeed: String,
    //chartData: LineChart
) {
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

@Preview
@Composable
fun PreviewStatisticsScreen() {
    StatisticsScreen(
        totalDistance = "0km",
        totalTime = "00:00:00",
        totalCalories = "0kcal",
        averageSpeed = "0km/h",
        //chartData = LineChart(null) // For preview purposes, placeholder chart
    )
}