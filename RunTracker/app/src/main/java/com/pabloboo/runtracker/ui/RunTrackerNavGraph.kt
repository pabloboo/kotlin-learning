package com.pabloboo.runtracker.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.github.mikephil.charting.charts.LineChart
import com.pabloboo.runtracker.ui.fragments.RunScreen
import com.pabloboo.runtracker.ui.fragments.SettingsScreen
import com.pabloboo.runtracker.ui.fragments.SetupScreen
import com.pabloboo.runtracker.ui.fragments.StatisticsScreen
import com.pabloboo.runtracker.ui.fragments.TrackingScreen
import com.pabloboo.runtracker.ui.fragments.sendCommandToService
import com.pabloboo.runtracker.utils.Constants.ACTION_START_SERVICE

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RunTrackerNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = RunTrackerDestinations.SETUP_SCREEN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(RunTrackerDestinations.SETUP_SCREEN) {
            SetupScreen(
                name = "Your name",
                weight = 70.0.toString(),
                onNameChange = { name -> /* logic to update name */ },
                onWeightChange = { weight -> /* logic to update weight */ },
                onContinueClick = { RunTrackerNavigationActions(navController).navigateToRunScreen() }
            )
        }

        composable(RunTrackerDestinations.RUN_SCREEN) {
            RunScreen(
                filterOptions = listOf("Distance", "Time", "Pace"),
                selectedFilter = "Distance",
                onFilterSelected = { filter -> /* logic to update selected filter */ },
                runs = listOf(/* run list */),
                onRunClick = { run -> /* details of the run */ },
                onAddRunClick = { RunTrackerNavigationActions(navController).navigateToTrackingScreen() }
            )
        }

        composable(RunTrackerDestinations.STATISTICS_SCREEN) {
            StatisticsScreen(
                totalDistance = "",
                totalTime = "",
                totalCalories = "",
                averageSpeed = ""
                //chartData =
            )
        }

        composable(RunTrackerDestinations.SETTINGS_SCREEN) {
            SettingsScreen(
                userName = "user name",
                userWeight = 70.0.toString(),
                onUserNameChange = { name -> /* logic to update user name */ },
                onUserWeightChange = { weight -> /* logic to update user weight */ },
                onApplyChanges = { /* logic to apply changes */ }
            )
        }

        composable(RunTrackerDestinations.TRACKING_SCREEN) {
            val context = LocalContext.current
            TrackingScreen(
                onToggleRun = {
                    /* logic to toggle run */
                    sendCommandToService(context, ACTION_START_SERVICE)
                },
                onFinishRun = { /* logic to finish run */ },
                onCancelRun = { RunTrackerNavigationActions(navController).navigateToRunScreen() },
                userName = "user name",
                isUserNameVisible = true,
            )
        }
    }
}
