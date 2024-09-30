package com.pabloboo.runtracker.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.pabloboo.runtracker.services.Polyline
import com.pabloboo.runtracker.services.TrackingService
import com.pabloboo.runtracker.utils.Constants.MAP_ZOOM
import com.pabloboo.runtracker.utils.Constants.POLYLINE_WIDTH
import com.pabloboo.runtracker.utils.TrackingUtility
import timber.log.Timber

@Composable
fun TrackingScreen(
    onToggleRun: () -> Unit,
    onFinishRun: () -> Unit,
    userName: String,
    isUserNameVisible: Boolean
) {

    // Observe the isTracking and the run time
    var isRunning by remember { mutableStateOf(false) }
    TrackingService.isTracking.observe(LocalLifecycleOwner.current) { isTracking ->
        isRunning = isTracking
    }

    var currentTimeInMillis: Long
    var timerText by remember { mutableStateOf("") }
    TrackingService.timeRunInMillis.observe(LocalLifecycleOwner.current) { timeInMillis ->
        currentTimeInMillis = timeInMillis
        val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis, true)
        timerText = formattedTime
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Map
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Map(paddingValues = PaddingValues(0.dp))
        }

        // Inner layout for controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1976D2))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Timer Text
            Text(
                text = timerText,
                fontSize = 50.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Toggle Run Button
            if (!isRunning) {
                Button(
                    onClick = {
                        onToggleRun()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(end = 8.dp)
                ) {
                    Text(text = "Start")
                }
            }

            // Finish Run Button
            if (isRunning) {
                Button(
                    onClick = {
                        onFinishRun()
                    },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(text = "Finish Run")
                }
            }
        }

        // Let's go Text
        if (isUserNameVisible) {
            Text(
                text = "Let's go, $userName!",
                fontSize = 50.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
fun Map(
    paddingValues: PaddingValues,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val pathPointsObserved = remember { TrackingService.pathPoints }
    var pathPointsValues by remember { mutableStateOf(mutableListOf<LatLng>()) }

    DisposableEffect(lifecycleOwner) {
        val observer = Observer<Polyline> { polyline ->
            // this block will be called whenever pathPointsObserved changes
            Timber.d("pathPoints changed: $polyline")
            if (polyline.isNotEmpty()) {
                pathPointsValues = pathPointsValues.toMutableList().apply {
                    add(polyline.last())
                }
            }
        }

        pathPointsObserved.observe(lifecycleOwner, observer)

        onDispose {
            pathPointsObserved.removeObserver(observer)
        }
    }

    var isMapLoaded by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()

    // Move the camera after the map is loaded and the points are updated
    LaunchedEffect(isMapLoaded, pathPointsValues) {
        if (isMapLoaded && pathPointsValues.isNotEmpty()) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(pathPointsValues.last(), MAP_ZOOM)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            onMapLoaded = {
                isMapLoaded = true
            },
            cameraPositionState = cameraPositionState,
        ) {
            Polyline(
                points = pathPointsValues,
                color = Color.Red,
                width = POLYLINE_WIDTH
            )
        }

        if (!isMapLoaded) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun sendCommandToService(context: Context, action: String) {
    val intent = Intent(context, TrackingService::class.java).apply {
        this.action = action
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}


@Preview
@Composable
fun PreviewTrackingScreen() {
    TrackingScreen(
        onToggleRun = {},
        onFinishRun = {},
        userName = "John Doe",
        isUserNameVisible = false
    )
}