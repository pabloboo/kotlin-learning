package com.pabloboo.runtracker.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.pabloboo.runtracker.db.Run
import com.pabloboo.runtracker.ui.viewmodels.StatisticsViewModel
import com.pabloboo.runtracker.utils.CustomMarkerView
import com.pabloboo.runtracker.utils.TrackingUtility
import kotlin.math.round

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel
) {
    val totalTimeRun = viewModel.totalTimeRun.observeAsState(initial = 0L)
    val totalDistance = viewModel.totalDistance.observeAsState(initial = 0)
    val totalCalories = viewModel.totalCaloriesBurned.observeAsState(initial = 0)
    val totalAvgSpeed = viewModel.totalAvgSpeed.observeAsState(initial = 0f)
    val runs = viewModel.runsSortedByDateAscending.observeAsState(initial = emptyList())

    val formattedTotalTime = TrackingUtility.getFormattedStopWatchTime(totalTimeRun.value)
    val formattedDistance = "${round(((totalDistance.value / 1000f) * 10f)) / 10f}km"
    val formattedCalories = "${totalCalories.value}kcal"
    val formattedSpeed = "${round(totalAvgSpeed.value * 10f) / 10f}km/h"

    val barData = runs.value.indices.map { i -> BarEntry(i.toFloat(), runs.value[i].distanceInMeters.toFloat()/1000f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Total Distance Section
        StatSection(
            title = "Total Distance",
            value = formattedDistance
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total Time Section
        StatSection(
            title = "Total Time",
            value = formattedTotalTime
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total Calories Section
        StatSection(
            title = "Total Calories Burned",
            value = formattedCalories
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Average Speed Section
        StatSection(
            title = "Average Speed",
            value = formattedSpeed
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bar Chart
        BarChartComposable(barData, runs.value)

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
fun BarChartComposable(chartData: List<BarEntry>, runs: List<Run>) {
    var selectedEntry by remember { mutableStateOf<BarEntry?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {
        AndroidView(
            factory = { context ->
                BarChart(context).apply {
                    val barDataSet = BarDataSet(chartData, "Distance Over Time").apply {
                        valueTextColor = Color.Black.toArgb()
                        color = Color(0xFFBB86FC).toArgb()
                    }
                    data = BarData(barDataSet)

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawLabels(false)
                        axisLineColor = Color.Black.toArgb()
                        textColor = Color.Black.toArgb()
                        setDrawGridLines(false)
                    }

                    axisLeft.apply {
                        axisLineColor = Color.Black.toArgb()
                        textColor = Color.Black.toArgb()
                        setDrawGridLines(false)
                    }

                    axisRight.apply {
                        axisLineColor = Color.Black.toArgb()
                        textColor = Color.Black.toArgb()
                        setDrawGridLines(false)
                    }

                    description.text = "Distance Over Time"
                    legend.isEnabled = false

                    invalidate() // Refresh the view

                    // Configure the listener to capture the selection of the BarEntry
                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(e: Entry?, h: Highlight?) {
                            selectedEntry = e as? BarEntry
                        }

                        override fun onNothingSelected() {
                            selectedEntry = null
                        }
                    })
                }
            },
            update = { chart ->
                chart.data = BarData(BarDataSet(chartData, "Distance Over Time"))
                chart.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Render the marker only if there is a selected `BarEntry`
        selectedEntry?.let { entry ->
            val curRun = runs[entry.x.toInt()]
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = (entry.x * 50).dp, top = 16.dp)
            ) {
                CustomMarkerView(curRun)
            }
        }
    }
}
