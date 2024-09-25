package com.pabloboo.runtracker.ui.fragments

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

@Composable
fun TrackingScreen(
    timerText: String,
    isRunning: Boolean,
    onToggleRun: () -> Unit,
    onFinishRun: () -> Unit,
    userName: String,
    isUserNameVisible: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Temporary placeholder for the map view
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Blue)
        )

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
            val toggleRunText = if (isRunning) "Stop" else "Start"
            Button(
                onClick = {
                    onToggleRun()
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(end = 8.dp)
            ) {
                Text(text = toggleRunText)
            }

            // Finish Run Button
            Button(
                onClick = {
                    onFinishRun()
                },
                modifier = Modifier.align(Alignment.BottomEnd),
                enabled = isRunning // Only enabled if running
            ) {
                Text(text = "Finish Run")
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

@Preview
@Composable
fun PreviewTrackingScreen() {
    TrackingScreen(
        timerText = "00:00:00:00",
        isRunning = false,
        onToggleRun = {},
        onFinishRun = {},
        userName = "John Doe",
        isUserNameVisible = false
    )
}