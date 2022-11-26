package com.jeffrwatts.celestialnavigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.SIGHT_ID_ARG
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.SIGHT_TITLE_ARG
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.USER_MESSAGE_ARG
import com.jeffrwatts.celestialnavigation.CelNavScreens.ADD_EDIT_SIGHT_SCREEN
import com.jeffrwatts.celestialnavigation.CelNavScreens.LOGS_SCREEN
import com.jeffrwatts.celestialnavigation.CelNavScreens.PLOT_SCREEN
import com.jeffrwatts.celestialnavigation.CelNavScreens.SIGHT_DETAIL_SCREEN

/**
 * Screens used in [CelNavDestinations]
 */
private object CelNavScreens {
    const val PLOT_SCREEN = "plot"
    const val LOGS_SCREEN = "logs"
    const val SIGHT_DETAIL_SCREEN = "sight"
    const val ADD_EDIT_SIGHT_SCREEN = "addEditSight"
}

/**
 * Arguments used in [CelNavDestinations] routes
 */
object CelNavDestinationsArgs {
    const val USER_MESSAGE_ARG = "userMessage"
    const val SIGHT_ID_ARG = "sightId"
    const val SIGHT_TITLE_ARG = "sightTitle"
}

/**
 * Destinations used in the [TasksActivity]
 */
object CelNavDestinations {
    const val PLOT_ROUTE = PLOT_SCREEN
    const val LOGS_ROUTE = LOGS_SCREEN
    const val SIGHT_DETAIL_ROUTE = "$SIGHT_DETAIL_SCREEN/{$SIGHT_ID_ARG}"
    const val ADD_EDIT_SIGHT_ROUTE = "$ADD_EDIT_SIGHT_SCREEN/{$SIGHT_TITLE_ARG}?$SIGHT_ID_ARG={$SIGHT_ID_ARG}"
}

class AppNavigationActions(private val navController: NavHostController) {

    fun navigateToPlot(userMessage: Int = 0) {
        val navigatesFromDrawer = userMessage == 0
        navController.navigate(
            PLOT_SCREEN.let {
                if (userMessage != 0) "$it?$USER_MESSAGE_ARG=$userMessage" else it
            }
        ) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = !navigatesFromDrawer
                saveState = navigatesFromDrawer
            }
            launchSingleTop = true
            restoreState = navigatesFromDrawer
        }
    }

    fun navigateToLogs() {
        navController.navigate(CelNavDestinations.LOGS_ROUTE) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }

    fun navigateToTaskDetail(sightId: String) {
        navController.navigate("$SIGHT_DETAIL_SCREEN/$sightId")
    }

    fun navigateToAddEditSight(title: Int, sightId: String?) {
        navController.navigate(
            "$ADD_EDIT_SIGHT_SCREEN/$title".let {
                if (sightId != null) "$it?$SIGHT_ID_ARG=$sightId" else it
            }
        )
    }
}
