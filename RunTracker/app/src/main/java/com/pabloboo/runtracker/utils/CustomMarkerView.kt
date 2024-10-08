package com.pabloboo.runtracker.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import com.pabloboo.runtracker.R
import com.pabloboo.runtracker.db.Run
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomMarkerView(
    run: Run
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = run.timestamp
    }
    val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
    val formattedDate = dateFormat.format(calendar.time)

    val avgSpeed = "${run.avgSpeedInKMH} km/h"
    val distanceInKm = "${run.distanceInMeters / 1000f} km"
    val duration = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
    val caloriesBurned = "${run.caloriesBurned} kcal"

    Surface(
        modifier = Modifier.padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = formattedDate, textAlign = TextAlign.Center, fontSize = 10.sp)
            //Text(text = "Avg Speed: $avgSpeed", textAlign = TextAlign.Center, fontSize = 10.sp)
            Text(text = getString(LocalContext.current, R.string.distance)+": $distanceInKm", textAlign = TextAlign.Center, fontSize = 10.sp)
            Text(text = getString(LocalContext.current, R.string.duration)+": $duration", textAlign = TextAlign.Center, fontSize = 10.sp)
            //Text(text = "Calories: $caloriesBurned", textAlign = TextAlign.Center, fontSize = 10.sp)
        }
    }
}