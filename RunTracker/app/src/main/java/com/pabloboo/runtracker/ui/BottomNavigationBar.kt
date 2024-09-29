package com.pabloboo.runtracker.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.pabloboo.runtracker.R

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    var navigationSelectedItem by remember { mutableIntStateOf(0) }

    // Listen for navigation changes
    var showBottomBar by remember { mutableStateOf(false) }
    navController.addOnDestinationChangedListener { _, destination, _ ->
        showBottomBar = destination.route in BottomNavigationItem.bottomNavigationItems().map { it.route }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavigationItem.bottomNavigationItems().forEachIndexed { index, navigationItem ->
                        NavigationBarItem(
                            selected = index == navigationSelectedItem,
                            label = { Text(navigationItem.label) },
                            icon = {
                                Icon(
                                    ImageVector.vectorResource(navigationItem.icon),
                                    contentDescription = navigationItem.label
                                )
                            },
                            onClick = {
                                navigationSelectedItem = index
                                navController.navigate(navigationItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        RunTrackerNavGraph(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

// Data class for Bottom Navigation Items
data class BottomNavigationItem(
    val label: String,
    val icon: Int,
    val route: String
) {
    companion object {
        // Function to get the list of bottom navigation items
        fun bottomNavigationItems(): List<BottomNavigationItem> {
            return listOf(
                BottomNavigationItem(
                    label = "Run",
                    icon = R.drawable.ic_run,
                    route = RunTrackerDestinations.RUN_SCREEN
                ),
                BottomNavigationItem(
                    label = "Statistics",
                    icon = R.drawable.ic_graph,
                    route = RunTrackerDestinations.STATISTICS_SCREEN
                ),
                BottomNavigationItem(
                    label = "Settings",
                    icon = R.drawable.ic_settings,
                    route = RunTrackerDestinations.SETTINGS_SCREEN
                )
            )
        }
    }
}