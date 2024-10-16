package com.pabloboo.runtracker.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import com.pabloboo.runtracker.R
import com.pabloboo.runtracker.db.Run
import com.pabloboo.runtracker.ui.viewmodels.MainViewModel
import com.pabloboo.runtracker.utils.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.pabloboo.runtracker.utils.Constants.SUCCESS_MESSAGE
import com.pabloboo.runtracker.utils.CustomSnackbarHost
import com.pabloboo.runtracker.utils.DataFunctions.kmhToMinPerKm
import com.pabloboo.runtracker.utils.SortType
import com.pabloboo.runtracker.utils.TrackingUtility.hasDeniedPermissionsPermanently
import com.pabloboo.runtracker.utils.TrackingUtility.hasLocationPermissions
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun RunScreen(
    onRunClick: (Run) -> Unit,
    onAddRunClick: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val filterOptions = listOf(
        getString(context, R.string.date),
        getString(context, R.string.time),
        getString(context, R.string.avg_speed),
        getString(context, R.string.distance),
        getString(context, R.string.calories_burned))
    var selectedFilter by remember { mutableStateOf(getString(context, R.string.date)) }

    val snackbarHostState = remember { SnackbarHostState() }

    when(viewModel.sortType) {
        SortType.DATE -> filterOptions[0]
        SortType.RUNNING_TIME -> filterOptions[1]
        SortType.AVG_SPEED -> filterOptions[2]
        SortType.DISTANCE -> filterOptions[3]
        SortType.CALORIES_BURNED -> filterOptions[4]
    }

    val runs by viewModel.runs.observeAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostStateMessages = remember { SnackbarHostState() }
    var snackbarMessageType by remember { mutableStateOf(SUCCESS_MESSAGE) }

    if (!hasLocationPermissions(context)) {
        requestPermissions(context)
    }

    if (!hasLocationPermissions(context) && hasDeniedPermissionsPermanently(context)) {
        // Show snackbar
        LaunchedEffect(Unit) {
            val result = snackbarHostState.showSnackbar(
                message = getString(context, R.string.need_to_accept_location_permissions),
                actionLabel = getString(context, R.string.open_settings)
            )
            if (result == SnackbarResult.ActionPerformed) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    val uri = android.net.Uri.fromParts("package", context.packageName, null)
                    data = uri
                }
                context.startActivity(intent)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermissions(context)) {
                        onAddRunClick()
                    } else {
                        requestPermissions(context)
                    }
                },
                containerColor = colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = getString(context, R.string.add_run),
                    tint = colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getString(context, R.string.sort_by),
                        fontSize = 16.sp,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DropdownMenu(
                        filterOptions = filterOptions,
                        selectedFilter = selectedFilter,
                        onFilterSelected = {
                            when(it) {
                                getString(context, R.string.date) -> viewModel.sortRuns(SortType.DATE)
                                getString(context, R.string.time) -> viewModel.sortRuns(SortType.RUNNING_TIME)
                                getString(context, R.string.avg_speed) -> viewModel.sortRuns(SortType.AVG_SPEED)
                                getString(context, R.string.distance) -> viewModel.sortRuns(SortType.DISTANCE)
                                getString(context, R.string.calories_burned) -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                                else -> viewModel.sortRuns(SortType.DATE)
                            }
                            // Change selected filter
                            selectedFilter = it
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                CustomSnackbarHost(
                    snackbarHostState = snackbarHostStateMessages,
                    snackbarMessageType = snackbarMessageType,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(runs.size) { run ->
                        RunItem(
                            run = runs[run],
                            onClick = onRunClick,
                            onDelete = {
                                coroutineScope.launch {
                                    snackbarMessageType = SUCCESS_MESSAGE
                                    snackbarHostStateMessages.showSnackbar(getString(context, R.string.run_deleted))
                                }
                            },
                            viewModel = viewModel)
                        Spacer(modifier = Modifier.height(10.dp))
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
fun RunItem(run: Run, onClick: (Run) -> Unit, onDelete: () -> Unit, viewModel: MainViewModel) {
    var showDeleteRunDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
            .padding(8.dp)
    ) {
        // Delete run button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = getString(LocalContext.current, R.string.delete_run),
                tint = colorScheme.error,
                modifier = Modifier
                    .clickable {
                        showDeleteRunDialog = true
                    }
            )
        }

        if (showDeleteRunDialog) {
            ShowDeleteRunDialog(
                onDismiss = { showDeleteRunDialog = false },
                viewModel = viewModel,
                onDelete = onDelete,
                run = run
            )
        }

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
        val date = dateFormatter.format(Date(run.timestamp))
        val time = formatMillisToTime(run.timeInMillis)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = date, fontSize = 16.sp, color = colorScheme.onSurface)
            Text(text = time, fontSize = 16.sp, color = colorScheme.onSurface)
            Text(text = "${run.distanceInMeters / 1000f} km", fontSize = 16.sp, color = colorScheme.onSurface)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "${kmhToMinPerKm(run.avgSpeedInKMH)} min/km", fontSize = 16.sp, color = colorScheme.onSurface)
            Text(text = "${run.avgSpeedInKMH} km/h", fontSize = 16.sp, color = colorScheme.onSurface)
            Text(text = "${run.caloriesBurned} cal", fontSize = 16.sp, color = colorScheme.onSurface)
        }
    }
}

@Composable
fun ShowDeleteRunDialog(onDismiss: () -> Unit, onDelete: () -> Unit, viewModel: MainViewModel, run: Run) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = getString(context, R.string.delete_the_run)) },
        text = { Text(text = getString(context, R.string.delete_the_run_confirmation)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = getString(context, R.string.delete),
                tint = colorScheme.tertiary
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    // Delete the run
                    viewModel.deleteRun(run)
                    onDelete()
                    onDismiss()
                }
            ) {
                Text(text = getString(context, R.string.yes))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    // Dismiss the dialog
                    onDismiss()
                }
            ) {
                Text(text = getString(context, R.string.no))
            }
        }
    )
}

fun formatMillisToTime(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}

private fun requestPermissions(context: Context) {
    if (hasLocationPermissions(context)) {
        return
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        EasyPermissions.requestPermissions(
            context as Activity,
            getString(context, R.string.need_to_accept_location_permissions),
            REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        EasyPermissions.requestPermissions(
            context as Activity,
            getString(context, R.string.need_to_accept_location_permissions),
            REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }
}