package com.pabloboo.runtracker.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.pabloboo.runtracker.utils.Constants.KEY_NAME
import com.pabloboo.runtracker.utils.Constants.KEY_WEIGHT
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
    viewModel: MainViewModel,
    sharedPref: SharedPreferences,
) {
    val name = sharedPref.getString(KEY_NAME, "") ?: ""
    val weight = sharedPref.getFloat(KEY_WEIGHT, 80f)
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
        modifier = Modifier.fillMaxSize().background(colorScheme.background)
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(colorScheme.primary)
        ) {
            Text(
                text = "Let's go, $name!",
                fontSize = 18.sp,
                textAlign = TextAlign.Left,
                color = colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(10.dp)
            )
            if (isRunning) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    tint = colorScheme.onPrimary,
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
                .height(70.dp)
                .background(colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            // Timer Text
            if (isRunning || runFinished) {
                Text(
                    text = timerText,
                    fontSize = 30.sp,
                    color = colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.CenterStart).padding(10.dp)
                )
            }

            // Toggle Run Button
            if (!isRunning && !runFinished) {
                Button(
                    onClick = {
                        onToggleRun()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.onPrimary,
                        contentColor = colorScheme.primary
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)
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
                                viewModel = viewModel,
                                weight = weight
                            )
                        }
                        onFinishRun()
                        runFinished = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.onPrimary,
                        contentColor = colorScheme.primary
                    ),
                    modifier = Modifier.align(Alignment.CenterEnd).padding(10.dp)
                ) {
                    Text(text = "Finish Run")
                }
            }
        }

        if (runFinished) {
            Snackbar(
                action = {
                    Button(
                        onClick = {
                            onGoBack()
                        }
                    ) {
                        Text(text = "Go to runs")
                    }
                },
                containerColor = colorScheme.secondary,
                contentColor = colorScheme.onSecondary
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
                color = colorScheme.tertiary,
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

private fun endRunAndSaveToDb(googleMap: GoogleMap, currentTimeInMillis: Long, viewModel: MainViewModel, weight: Float) {
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
                tint = colorScheme.tertiary
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
        viewModel = viewModel(),
        sharedPref = LocalContext.current.getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)
    )
}