package com.pabloboo.runtracker.ui

import android.content.SharedPreferences
import android.os.Build
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.pabloboo.runtracker.ui.viewmodels.MainViewModel
import com.pabloboo.runtracker.ui.viewmodels.StatisticsViewModel
import com.pabloboo.runtracker.utils.Constants.ACTION_START_SERVICE
import com.pabloboo.runtracker.utils.Constants.ACTION_STOP_SERVICE

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RunTrackerNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = RunTrackerDestinations.SETUP_SCREEN,
    viewModel: MainViewModel,
    statisticsViewModel: StatisticsViewModel,
    sharedPref: SharedPreferences
) {

    // Handle back press navigation -> only allow back navigation if the current destination is not the start destination
    val backCallback = remember {
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.route != navController.graph.findStartDestination().route) {
                    navController.popBackStack()
                }
            }
        }
    }

    BackHandler(onBack = {
        if (navController.currentDestination?.route != RunTrackerDestinations.RUN_SCREEN) {
            backCallback.handleOnBackPressed()
        }
    })

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(RunTrackerDestinations.SETUP_SCREEN) {
            SetupScreen(
                sharedPref = sharedPref,
                goNextScreen = { RunTrackerNavigationActions(navController).navigateToRunScreen() }
            )
        }

        composable(RunTrackerDestinations.RUN_SCREEN) {
            RunScreen(
                onRunClick = { run -> /* details of the run */ },
                onAddRunClick = { RunTrackerNavigationActions(navController).navigateToTrackingScreen() },
                viewModel = viewModel
            )
        }

        composable(RunTrackerDestinations.STATISTICS_SCREEN) {
            StatisticsScreen(
                viewModel = statisticsViewModel
                //chartData =
            )
        }

        composable(RunTrackerDestinations.SETTINGS_SCREEN) {
            SettingsScreen(
                sharedPref = sharedPref,
                onFinishSaving = {
                    navController.popBackStack(navController.graph.startDestinationId, false)
                    RunTrackerNavigationActions(navController).navigateToRunScreen()
                }
            )
        }

        composable(RunTrackerDestinations.TRACKING_SCREEN) {
            val context = LocalContext.current
            TrackingScreen(
                onToggleRun = {
                    /* logic to toggle run */
                    sendCommandToService(context, ACTION_START_SERVICE)
                },
                onFinishRun = {
                    /* logic to finish run */
                    sendCommandToService(context, ACTION_STOP_SERVICE)
                },
                onGoBack = { RunTrackerNavigationActions(navController).navigateToRunScreen() },
                viewModel = viewModel,
                sharedPref = sharedPref
            )
        }
    }
}
