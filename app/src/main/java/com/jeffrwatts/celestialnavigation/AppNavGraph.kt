package com.jeffrwatts.celestialnavigation

import android.app.Activity
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.CELESTIAL_BODY_ARG
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.USER_MESSAGE_ARG
import com.jeffrwatts.celestialnavigation.addsight.AddSightScreen
import com.jeffrwatts.celestialnavigation.celestialbody.CelestialBodyScreen
import com.jeffrwatts.celestialnavigation.plotting.PlotScreen
import com.jeffrwatts.celestialnavigation.sights.SightsScreen
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    startDestination: String = CelNavDestinations.PLOT_ROUTE,
    navActions: AppNavigationActions = remember(navController) {
        AppNavigationActions(navController)
    }
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            CelNavDestinations.PLOT_ROUTE) {
            PlotScreen(
                onAddSight = { navActions.navigateToCelestialBodies() },
                onEditSights = { navActions.navigateToSights() })
        }
        composable(
            CelNavDestinations.ADD_EDIT_SIGHT_ROUTE,
            arguments = listOf(
                navArgument(CELESTIAL_BODY_ARG) { type = NavType.StringType }
            )
        ) { entry ->
            AddSightScreen(
                onSightUpdate = { navActions.navigateToPlot() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            CelNavDestinations.SIGHTS_ROUTE,
            arguments = listOf(
                navArgument(USER_MESSAGE_ARG) { type = NavType.IntType; defaultValue = 0 }
            )
        ) { entry ->
            SightsScreen(
                userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
                onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
                onAddSight = { navActions.navigateToCelestialBodies() },
                onSightClick = {  },
                onBack = { navController.popBackStack() }
            )
        }
        composable(CelNavDestinations.CELESTIAL_BODY_ROUTE) {
            CelestialBodyScreen(onBack = {navController.popBackStack()},
                onClick = { celestialBody -> navActions.navigateToAddEditSight(celestialBody) })
        }
    }
}

// Keys for navigation
const val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3
