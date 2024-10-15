package com.pabloboo.runtracker.ui.fragments

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import com.pabloboo.runtracker.R
import com.pabloboo.runtracker.ui.viewmodels.MainViewModel
import com.pabloboo.runtracker.utils.Constants.ERROR_MESSAGE
import com.pabloboo.runtracker.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.pabloboo.runtracker.utils.Constants.KEY_NAME
import com.pabloboo.runtracker.utils.Constants.KEY_WEIGHT
import com.pabloboo.runtracker.utils.Constants.SUCCESS_MESSAGE
import com.pabloboo.runtracker.utils.CustomSnackbarHost
import com.pabloboo.runtracker.utils.ExportAndImportData.exportRunsToJson
import com.pabloboo.runtracker.utils.ExportAndImportData.importRunsFromJson
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    sharedPref: SharedPreferences,
    viewModel: MainViewModel
) {
    var name by remember { mutableStateOf(sharedPref.getString(KEY_NAME, "") ?: "") }
    var weight by remember { mutableStateOf(sharedPref.getFloat(KEY_WEIGHT, 80f).toString()) }

    fun writePersonalDataToSharedPref(): Boolean {
        if (name.isNotEmpty() && weight.isNotEmpty() && name.isNotBlank() && weight.isNotBlank() && weight.toFloatOrNull() != null) {
            sharedPref.edit()
                .putString(KEY_NAME, name)
                .putFloat(KEY_WEIGHT, weight.toFloat())
                .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
                .apply()
            return true
        }
        return false
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val runs by viewModel.runs.observeAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessageType by remember { mutableStateOf(SUCCESS_MESSAGE) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val importMessage = importRunsFromJson(context, viewModel, it)
                snackbarMessageType = importMessage.type
                snackbarHostState.showSnackbar(importMessage.message)
            }
        }
    }

    // Launcher para solicitar el permiso de escritura
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            coroutineScope.launch {
                val exportMessage = exportRunsToJson(context, runs)
                snackbarMessageType = exportMessage.type
                snackbarHostState.showSnackbar(exportMessage.message)
            }
        } else {
            coroutineScope.launch {
                snackbarMessageType = ERROR_MESSAGE
                snackbarHostState.showSnackbar(getString(context, R.string.write_permission_denied))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Name Input Field
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = { Text(getString(context, R.string.your_name)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Weight Input Field
        OutlinedTextField(
            value = weight,
            onValueChange = {
                weight = it
            },
            label = { Text(getString(context, R.string.your_weight)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Apply Changes Button
        Button(
            onClick = {
                if (writePersonalDataToSharedPref()) {
                    // Show success message
                    coroutineScope.launch {
                        snackbarMessageType = SUCCESS_MESSAGE
                        snackbarHostState.showSnackbar(getString(context, R.string.changes_saved))
                    }
                } else {
                    // Show error message
                    coroutineScope.launch {
                        snackbarMessageType = ERROR_MESSAGE
                        snackbarHostState.showSnackbar(getString(context, R.string.fill_in_all_fields_correctly))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
        ) {
            Text(text = getString(context, R.string.apply_changes))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when(PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                        coroutineScope.launch {
                            val exportMessage = exportRunsToJson(context, runs)
                            snackbarMessageType = exportMessage.type
                            snackbarHostState.showSnackbar(exportMessage.message)
                        }
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
        ) {
            Text(text = getString(context, R.string.export_runs))
        }

        Button(
            onClick = {
                importLauncher.launch("*/*")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
        ) {
            Text(text = getString(context, R.string.import_runs))
        }

        CustomSnackbarHost(
            snackbarHostState = snackbarHostState,
            snackbarMessageType = snackbarMessageType,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}