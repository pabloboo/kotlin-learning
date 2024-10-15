package com.pabloboo.runtracker.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pabloboo.runtracker.utils.Constants.ERROR_MESSAGE

@Composable
fun CustomSnackbarHost(
    snackbarHostState: SnackbarHostState,
    snackbarMessageType: String,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = snackbarHostState,
        snackbar = { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = if (snackbarMessageType == ERROR_MESSAGE) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
                contentColor = if (snackbarMessageType == ERROR_MESSAGE) {
                    MaterialTheme.colorScheme.onTertiary
                } else {
                    MaterialTheme.colorScheme.onSecondary
                }
            )
        },
        modifier = modifier
    )
}
