package com.jeffrwatts.celestialnavigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.SIGHT_ID_ARG
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.TITLE_ARG
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.USER_MESSAGE_ARG
import com.jeffrwatts.celestialnavigation.CelNavScreens.ADD_EDIT_SIGHT_SCREEN
import com.jeffrwatts.celestialnavigation.CelNavScreens.SIGHTS_SCREEN
import com.jeffrwatts.celestialnavigation.CelNavScreens.PLOT_SCREEN
import com.jeffrwatts.celestialnavigation.CelNavScreens.SIGHT_DETAIL_SCREEN

/**
 * Screens used in [CelNavDestinations]
 */
private object CelNavScreens {
    const val SIGHTS_SCREEN = "sights"
    const val PLOT_SCREEN = "plot"
    const val SIGHT_DETAIL_SCREEN = "sight"
    const val ADD_EDIT_SIGHT_SCREEN = "addEditSight"
}

/**
 * Arguments used in [CelNavDestinations] routes
 */
object CelNavDestinationsArgs {
    const val USER_MESSAGE_ARG = "userMessage"
    const val SIGHT_ID_ARG = "sightId"
    const val TITLE_ARG = "title"
}

/**
 * Destinations used in the [TasksActivity]
 */
object CelNavDestinations {
    const val SIGHTS_ROUTE = "$SIGHTS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
    const val PLOT_ROUTE = PLOT_SCREEN
    const val SIGHT_DETAIL_ROUTE = "$SIGHT_DETAIL_SCREEN/{$SIGHT_ID_ARG}"
    const val ADD_EDIT_SIGHT_ROUTE = "$ADD_EDIT_SIGHT_SCREEN/{$TITLE_ARG}?$SIGHT_ID_ARG={$SIGHT_ID_ARG}"
}

/**
 * Models the navigation actions in the app.
 */
class AppNavigationActions(private val navController: NavHostController) {

    fun navigateToSights(userMessage: Int = 0) {
        val navigatesFromDrawer = userMessage == 0
        navController.navigate(
            SIGHTS_SCREEN.let {
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

    fun navigateToPlot(userMessage: Int = 0) {
        val navigatesFromDrawer = userMessage == 0

        navController.navigate(
            PLOT_SCREEN.let {
                if (userMessage != 0) "$it?$USER_MESSAGE_ARG=$userMessage" else it
            }
        ) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = !navigatesFromDrawer
                saveState = navigatesFromDrawer
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = navigatesFromDrawer
        }
    }

    fun navigateToSightDetail(sightId: String) {
        navController.navigate("$SIGHT_DETAIL_SCREEN/$sightId")
    }

    fun navigateToAddEditSight(title: Int, taskId: String?) {
        navController.navigate(
            "$ADD_EDIT_SIGHT_SCREEN/$title".let {
                if (taskId != null) "$it?$SIGHT_ID_ARG=$taskId" else it
            }
        )
    }
}