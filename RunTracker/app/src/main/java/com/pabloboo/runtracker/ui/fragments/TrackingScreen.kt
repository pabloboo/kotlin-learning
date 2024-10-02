package com.pabloboo.runtracker.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.pabloboo.runtracker.R
import com.pabloboo.runtracker.db.Run
import com.pabloboo.runtracker.services.Polyline
import com.pabloboo.runtracker.services.TrackingService
import com.pabloboo.runtracker.ui.viewmodels.MainViewModel
import com.pabloboo.runtracker.utils.Constants.ACTION_STOP_SERVICE
import com.pabloboo.runtracker.utils.Constants.MAP_ZOOM
import com.pabloboo.runtracker.utils.Constants.POLYLINE_WIDTH
import com.pabloboo.runtracker.utils.TrackingUtility
import timber.log.Timber
import java.util.Calendar
import kotlin.math.round

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TrackingScreen(
    onToggleRun: () -> Unit,
    onFinishRun: () -> Unit,
    onGoBack: () -> Unit,
    userName: String,
    isUserNameVisible: Boolean,
    viewModel: MainViewModel
) {
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var runFinished by remember { mutableStateOf(false) }

    // Observe the isTracking and the run time
    var isRunning by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    TrackingService.isTracking.observe(LocalLifecycleOwner.current) { isTracking ->
        isRunning = isTracking
    }

    var currentTimeInMillis: Long = 0
    var timerText by remember { mutableStateOf("") }
    TrackingService.timeRunInMillis.observe(LocalLifecycleOwner.current) { timeInMillis ->
        currentTimeInMillis = timeInMillis
        val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis, true)
        timerText = formattedTime
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        if (isRunning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color(0xFF1976D2))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Cancel Run",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(10.dp)
                        .clickable(onClick = {
                            showCancelDialog = true
                        })
                )
            }
        }

        if (showCancelDialog) (
            ShowCancelTrackingDialog(
                onDismiss = { showCancelDialog = false },
                onGoBack = onGoBack
            )
        )

        // Map
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Map(paddingValues = PaddingValues(0.dp)) { map ->
                googleMap = map
            }
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
            if (!isRunning && !runFinished) {
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
                        googleMap?.let {
                            zoomToSeeWholeTrack(
                                googleMap = it
                            )
                        }
                        if (currentTimeInMillis > 0) {
                            endRunAndSaveToDb(
                                googleMap = googleMap!!,
                                currentTimeInMillis = currentTimeInMillis,
                                viewModel = viewModel
                            )
                        }
                        onFinishRun()
                        runFinished = true
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

        if (runFinished) {
            Snackbar(
                action = {
                    Button(
                        onClick = {
                            onGoBack()
                        }
                    ) {
                        Text(text = "Go back")
                    }
                }
            ) {
                Text(text = "Run Finished!")
            }
        }
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun Map(
    paddingValues: PaddingValues,
    onMapReady: (GoogleMap) -> Unit
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

            MapEffect(Unit) { map ->
                onMapReady(map)
            }
        }

        if (!isMapLoaded) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

private fun zoomToSeeWholeTrack(googleMap: GoogleMap)  {
    val bounds = LatLngBounds.Builder()
    TrackingService.pathPoints.value?.forEach { polyline ->
        bounds.include(polyline)
    }

    // Move the camera to the bounds
    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50))
}

private fun endRunAndSaveToDb(googleMap: GoogleMap, currentTimeInMillis: Long, viewModel: MainViewModel) {
    val weight = 80f

    googleMap.snapshot { bitmap ->
        val distanceInMeters = TrackingService.pathPoints.value?.let {
            TrackingUtility.calculatePolylineLength(
                it
            ).toInt()
        }
        val avgSpeed = (distanceInMeters?.div(1000f))?.div((currentTimeInMillis / 1000f / 60 /60) * 10)
            ?.let { round(it) }?.div(10f)
        val dateTimestamp = Calendar.getInstance().timeInMillis
        val caloriesBurned = ((distanceInMeters?.div(1000f))?.times(weight))?.toInt()

        val run = Run(bitmap, dateTimestamp, avgSpeed!!, distanceInMeters, currentTimeInMillis, caloriesBurned!!)
        viewModel.insertRun(run)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowCancelTrackingDialog(onDismiss: () -> Unit, onGoBack: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Cancel the Run") },
        text = { Text(text = "Are you sure you want to cancel the run and delete all its data?") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = "Delete",
                tint = Color.Red
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    // Cancel the run
                    stopRun(context)
                    onGoBack()
                }
            ) {
                Text(text = "Yes")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    // Dismiss the dialog
                    onDismiss()
                }
            ) {
                Text(text = "No")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun stopRun(context: Context) {
    sendCommandToService(
        context = context,
        action = ACTION_STOP_SERVICE
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun PreviewTrackingScreen() {
    TrackingScreen(
        onToggleRun = {},
        onFinishRun = {},
        onGoBack = {},
        userName = "John Doe",
        isUserNameVisible = false,
        viewModel = viewModel()
    )
}