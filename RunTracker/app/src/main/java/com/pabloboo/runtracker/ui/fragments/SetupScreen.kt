package com.pabloboo.runtracker.ui.fragments

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import com.pabloboo.runtracker.R
import com.pabloboo.runtracker.utils.Constants.ERROR_MESSAGE
import com.pabloboo.runtracker.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.pabloboo.runtracker.utils.Constants.KEY_NAME
import com.pabloboo.runtracker.utils.Constants.KEY_WEIGHT
import com.pabloboo.runtracker.utils.Constants.SUCCESS_MESSAGE
import com.pabloboo.runtracker.utils.CustomSnackbarHost
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(
    sharedPref: SharedPreferences,
    goNextScreen: () -> Unit
) {
    val isFirstAppOpen = sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
    if (!isFirstAppOpen) {
        goNextScreen()
        return
    }

    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessageType by remember { mutableStateOf(SUCCESS_MESSAGE) }

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
    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background)
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = getString(context, R.string.welcome),
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    color = colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = { Text(getString(context, R.string.your_name)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = {
                            weight = it
                        },
                        label = { Text(getString(context, R.string.your_weight)) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )
                    Text(
                        text = getString(context, R.string.kg),
                        fontSize = 24.sp,
                        color = colorScheme.onBackground,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row (
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (writePersonalDataToSharedPref()) {
                                goNextScreen()
                            } else {
                                coroutineScope.launch {
                                    snackbarMessageType = ERROR_MESSAGE
                                    snackbarHostState.showSnackbar(getString(context, R.string.fill_in_all_fields_correctly))
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                    ) {
                        Text(
                            getString(context, R.string.continue_text),
                            fontSize = 22.sp,
                            color = colorScheme.onPrimary
                        )
                    }
                }

                CustomSnackbarHost(
                    snackbarHostState = snackbarHostState,
                    snackbarMessageType = snackbarMessageType,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    )
}
