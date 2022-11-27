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
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.SIGHT_ID_ARG
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.TITLE_ARG
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.USER_MESSAGE_ARG
import com.jeffrwatts.celestialnavigation.addeditsight.AddEditSightScreen
import com.jeffrwatts.celestialnavigation.plotting.PlotScreen
import com.jeffrwatts.celestialnavigation.sights.SightsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    startDestination: String = CelNavDestinations.SIGHTS_ROUTE,
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
            CelNavDestinations.SIGHTS_ROUTE,
            arguments = listOf(
                navArgument(USER_MESSAGE_ARG) { type = NavType.IntType; defaultValue = 0 }
            )
        ) { entry ->
            AppModalDrawer(drawerState, currentRoute, navActions) {
                SightsScreen(
                    userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
                    onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
                    onAddSight = { navActions.navigateToAddEditSight(R.string.add_sight, null) },
                    onSightClick = { sight -> navActions.navigateToSightDetail(sight.id) },
                    openDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
        }
        composable(CelNavDestinations.PLOT_ROUTE) {
            AppModalDrawer(drawerState, currentRoute, navActions) {
                PlotScreen(
                    onAddSight = { navActions.navigateToAddEditSight(R.string.add_sight, null) },
                    openDrawer = { coroutineScope.launch { drawerState.open() } })
            }
        }
        composable(
            CelNavDestinations.ADD_EDIT_SIGHT_ROUTE,
            arguments = listOf(
                navArgument(TITLE_ARG) { type = NavType.IntType },
                navArgument(SIGHT_ID_ARG) { type = NavType.StringType; nullable = true },
            )
        ) { entry ->
            val taskId = entry.arguments?.getString(SIGHT_ID_ARG)
            AddEditSightScreen(
                topBarTitle = entry.arguments?.getInt(TITLE_ARG)!!,
                onSightUpdate = {
                    navActions.navigateToSights(
                        if (taskId == null) ADD_EDIT_RESULT_OK else EDIT_RESULT_OK
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(CelNavDestinations.SIGHT_DETAIL_ROUTE) {
            //TaskDetailScreen(
            //    onEditTask = { sightId ->
            //        navActions.navigateToAddEditSight(R.string.edit_sight, sightId)
            //    },
            //    onBack = { navController.popBackStack() },
            //    onDeleteTask = { navActions.navigateToSights(DELETE_RESULT_OK) }
            //)
        }
    }
}

// Keys for navigation
const val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3
