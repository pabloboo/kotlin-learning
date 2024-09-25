package com.pabloboo.runtracker.ui

import androidx.navigation.NavHostController

object RunTrackerDestinations {
    const val SETUP_SCREEN = "setup_screen"
    const val RUN_SCREEN = "run_screen"
    const val STATISTICS_SCREEN = "statistics_screen"
    const val SETTINGS_SCREEN = "settings_screen"
    const val TRACKING_SCREEN = "tracking_screen"
}

class RunTrackerNavigationActions(navController: NavHostController) {
    val navigateToSetupScreen: () -> Unit = {
        navController.navigate(RunTrackerDestinations.SETUP_SCREEN)
    }

    val navigateToRunScreen: () -> Unit = {
        navController.navigate(RunTrackerDestinations.RUN_SCREEN)
    }

    val navigateToStatisticsScreen: () -> Unit = {
        navController.navigate(RunTrackerDestinations.STATISTICS_SCREEN)
    }

    val navigateToSettingsScreen: () -> Unit = {
        navController.navigate(RunTrackerDestinations.SETTINGS_SCREEN)
    }

    val navigateToTrackingScreen: () -> Unit = {
        navController.navigate(RunTrackerDestinations.TRACKING_SCREEN)
    }
}
