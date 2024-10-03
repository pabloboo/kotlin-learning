package com.pabloboo.runtracker.ui.fragments

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pabloboo.runtracker.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.pabloboo.runtracker.utils.Constants.KEY_NAME
import com.pabloboo.runtracker.utils.Constants.KEY_WEIGHT

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
    var showSnackbarError by remember { mutableStateOf(false) }

    fun writePersonalDataToSharedPref(): Boolean {
        if (name.isNotEmpty() && weight.isNotEmpty() && name.isNotBlank() && weight.isNotBlank()) {
            sharedPref.edit()
                .putString(KEY_NAME, name)
                .putFloat(KEY_WEIGHT, weight.toFloat())
                .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
                .apply()
            return true
        }
        return false
    }

    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome!\nPlease enter your name and weight.",
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = { Text("Your Name") },
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
                        label = { Text("Your Weight") },
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
                        text = "kg",
                        fontSize = 24.sp
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
                                showSnackbarError = true
                            }
                        }
                    ) {
                        Text(
                            "Continue",
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    }
                }

                if (showSnackbarError) {
                    Snackbar() {
                        Text(text = "Fill in all the fields!")
                    }
                }
            }
        }
    )
}
