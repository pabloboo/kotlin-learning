package com.pabloboo.runtracker.ui.fragments

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import com.pabloboo.runtracker.R
import com.pabloboo.runtracker.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.pabloboo.runtracker.utils.Constants.KEY_NAME
import com.pabloboo.runtracker.utils.Constants.KEY_WEIGHT

@Composable
fun SettingsScreen(
    sharedPref: SharedPreferences,
    onFinishSaving: () -> Unit
) {
    var name by remember { mutableStateOf(sharedPref.getString(KEY_NAME, "") ?: "") }
    var weight by remember { mutableStateOf(sharedPref.getFloat(KEY_WEIGHT, 80f).toString()) }
    var showSnackbarError by remember { mutableStateOf(false) }
    var showSnackBarSuccess by remember { mutableStateOf(false) }

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
                    showSnackbarError = false
                    showSnackBarSuccess = true
                    onFinishSaving()
                } else {
                    // Show error message
                    showSnackbarError = true
                    showSnackBarSuccess = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
        ) {
            Text(text = getString(context, R.string.apply_changes))
        }

        if (showSnackbarError) {
            Snackbar(
                containerColor = colorScheme.tertiary,
                contentColor = colorScheme.onTertiary
            ) {
                Text(text = getString(context, R.string.fill_in_all_fields_correctly))
            }
        }
        if (showSnackBarSuccess) {
            Snackbar(
                containerColor = colorScheme.secondary,
                contentColor = colorScheme.onSecondary
            ) {
                Text(text = getString(context, R.string.changes_saved))
            }
        }
    }
}