package com.pabloboo.runtracker.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pabloboo.runtracker.ui.theme.RunTrackerTheme
import com.pabloboo.runtracker.utils.Constants.ACTION_SHOW_TRACKING_SCREEN
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var startDestination by mutableStateOf(RunTrackerDestinations.SETUP_SCREEN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Restore startDestination from savedInstanceState if available
        savedInstanceState?.let {
            startDestination = it.getString("startDestination") ?: RunTrackerDestinations.SETUP_SCREEN
        }

        handleIntent(intent)

        setContent {
            RunTrackerTheme {
                MainScreen(startDestination)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_SCREEN) {
            startDestination = RunTrackerDestinations.TRACKING_SCREEN
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("startDestination", startDestination)
    }
}

@Composable
fun MainScreen(startDestination: String) {
    val navController = rememberNavController()

    // Use a Scaffold to include the BottomNavigationBar
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        RunTrackerNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            startDestination = startDestination
        )
    }
}
