package com.pabloboo.runtracker.ui.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.getString
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.pabloboo.runtracker.R
import com.pabloboo.runtracker.db.Run
import com.pabloboo.runtracker.ui.viewmodels.StatisticsViewModel
import com.pabloboo.runtracker.utils.CustomMarkerView
import com.pabloboo.runtracker.utils.DataFunctions.getTotalKmOfCurrentWeek
import com.pabloboo.runtracker.utils.DataFunctions.getTotalKmOfPastWeek
import com.pabloboo.runtracker.utils.DataFunctions.kmhToMinPerKm
import com.pabloboo.runtracker.utils.TrackingUtility
import kotlin.math.round

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel
) {
    val totalTimeRun = viewModel.totalTimeRun.observeAsState()
    val totalDistance = viewModel.totalDistance.observeAsState()
    val totalNumberOfRuns = viewModel.totalNumberOfRuns.observeAsState()
    val totalCalories = viewModel.totalCaloriesBurned.observeAsState()
    val totalAvgSpeed = viewModel.totalAvgSpeed.observeAsState()
    val runs = viewModel.runsSortedByDateAscending.observeAsState(initial = emptyList())
    val get5000MetersRecord = viewModel.get5000MetersRecord.observeAsState()
    val get10000MetersRecord = viewModel.get10000MetersRecord.observeAsState()
    val get21000MetersRecord = viewModel.get21000MetersRecord.observeAsState()

    var formattedTotalTime = "00:00:00"
    if (totalTimeRun.value != null) {
        formattedTotalTime = TrackingUtility.getFormattedStopWatchTime(totalTimeRun.value!!)
    }
    var formattedDistance = "0km"
    if (totalDistance.value != null) {
        formattedDistance = "${round(((totalDistance.value!! / 1000f) * 10f)) / 10f}km"
    }
    var formattedNumberOfRuns = "0"
    if (totalNumberOfRuns.value != null) {
        formattedNumberOfRuns = "${totalNumberOfRuns.value}"
    }
    var formattedCalories = "0kcal"
    if (totalCalories.value != null) {
        formattedCalories = "${totalCalories.value}kcal"
    }
    var formattedSpeed = "0km/h"
    if (totalAvgSpeed.value != null) {
        formattedSpeed = "${round(totalAvgSpeed.value!! * 10f) / 10f}km/h"
    }
    var formattedMinKm = "0min/km"
    if (totalAvgSpeed.value != null) {
        formattedMinKm = "${round(kmhToMinPerKm(totalAvgSpeed.value!!) * 10f) / 10f}min/km"
    }

    val barData = runs.value.indices.map { i -> BarEntry(i.toFloat(), runs.value[i].distanceInMeters.toFloat()/1000f) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Total Distance Section
        StatSection(
            title = getString(context, R.string.total_distance),
            value = formattedDistance
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total Time Section
        StatSection(
            title = getString(context, R.string.total_time),
            value = formattedTotalTime
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total Number of Runs Section
        StatSection(
            title = getString(context, R.string.total_number_of_runs),
            value = formattedNumberOfRuns
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total Calories Section
        StatSection(
            title = getString(context, R.string.total_calories_burned),
            value = formattedCalories
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Min/km Section
        StatSection(
            title = getString(context, R.string.average_time_per_km),
            value = formattedMinKm
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Average Speed Section
        StatSection(
            title = getString(context, R.string.average_speed),
            value = formattedSpeed
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bar Chart
        BarChartComposable(barData, runs.value)

        Spacer(modifier = Modifier.height(24.dp))

        // Current week's total distance Section
        StatSection(
            title = getString(context, R.string.current_week_total_distance),
            value = getTotalKmOfCurrentWeek(runs.value).toString() + "km"
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "${getTotalKmOfCurrentWeek(runs.value) - getTotalKmOfPastWeek(runs.value)}km " + getString(context, R.string.last_week_total_distance),
            fontSize = 16.sp,
            color = colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Records
        RecordSection(
            title = getString(context, R.string.record_5k),
            value = if (get5000MetersRecord.value != null) {
                "${TrackingUtility.getFormattedStopWatchTime(get5000MetersRecord.value!!.timeInMillis)} - ${round(((get5000MetersRecord.value!!.distanceInMeters / 1000f) * 10f)) / 10f}km"
            } else {
                getString(context, R.string.no_record)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RecordSection(
            title = getString(context, R.string.record_10k),
            value = if (get10000MetersRecord.value != null) {
                "${TrackingUtility.getFormattedStopWatchTime(get10000MetersRecord.value!!.timeInMillis)} - ${round(((get10000MetersRecord.value!!.distanceInMeters / 1000f) * 10f)) / 10f}km"
            } else {
                getString(context, R.string.no_record)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RecordSection(
            title = getString(context, R.string.record_21k),
            value = if (get21000MetersRecord.value != null) {
                "${TrackingUtility.getFormattedStopWatchTime(get21000MetersRecord.value!!.timeInMillis)} - ${round(((get21000MetersRecord.value!!.distanceInMeters / 1000f) * 10f)) / 10f}km"
            } else {
                getString(context, R.string.no_record)
            }
        )

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
            color = colorScheme.onBackground
        )
        Text(
            text = title,
            fontSize = 16.sp,
            color = colorScheme.onBackground
        )
    }
}

@Composable
fun RecordSection(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )
        Text(
            text = title,
            fontSize = 16.sp,
            color = colorScheme.onBackground
        )
    }
}

@Composable
fun BarChartComposable(chartData: List<BarEntry>, runs: List<Run>) {
    var selectedEntry by remember { mutableStateOf<BarEntry?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        val onBackgroundColor = colorScheme.onBackground.toArgb()
        val contextVar = LocalContext.current
        AndroidView(
            factory = { context ->
                BarChart(context).apply {
                    val barDataSet = BarDataSet(chartData, getString(context, R.string.distance_over_time)).apply {
                        valueTextColor = onBackgroundColor
                        color = onBackgroundColor
                    }
                    data = BarData(barDataSet)

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawLabels(false)
                        axisLineColor = onBackgroundColor
                        textColor = onBackgroundColor
                        setDrawGridLines(false)
                    }

                    axisLeft.apply {
                        axisLineColor = onBackgroundColor
                        textColor = onBackgroundColor
                        setDrawGridLines(false)
                    }

                    axisRight.apply {
                        axisLineColor = onBackgroundColor
                        textColor = onBackgroundColor
                        setDrawGridLines(false)
                    }

                    description.text = getString(context, R.string.distance_over_time)
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
                chart.data = BarData(BarDataSet(chartData, getString(contextVar, R.string.distance_over_time)))
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
                    .padding(start = 50.dp, top = 16.dp)
            ) {
                CustomMarkerView(curRun)
            }
        }
    }
}
