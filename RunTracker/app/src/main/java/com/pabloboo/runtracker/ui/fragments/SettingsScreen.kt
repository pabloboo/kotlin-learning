package com.pabloboo.runtracker.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    userName: String,
    userWeight: String,
    onUserNameChange: (String) -> Unit,
    onUserWeightChange: (String) -> Unit,
    onApplyChanges: () -> Unit
) {
    var name by remember { mutableStateOf(userName) }
    var weight by remember { mutableStateOf(userWeight) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Name Input Field
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                onUserNameChange(it)
            },
            label = { Text("Your Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Weight Input Field
        OutlinedTextField(
            value = weight,
            onValueChange = {
                weight = it
                onUserWeightChange(it)
            },
            label = { Text("Your Weight") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Apply Changes Button
        Button(
            onClick = { onApplyChanges() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply Changes")
        }
    }
}

@Preview
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen(
        userName = "John Doe",
        userWeight = "70",
        onUserNameChange = {},
        onUserWeightChange = {},
        onApplyChanges = {}
    )
}