package com.jeffrwatts.celestialnavigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.jeffrwatts.celestialnavigation.CelNavDestinations.CELESTIAL_BODY_ROUTE
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.CELESTIAL_BODY_ARG
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs.USER_MESSAGE_ARG
import com.jeffrwatts.celestialnavigation.CelNavScreens.ADD_EDIT_SIGHT_SCREEN
import com.jeffrwatts.celestialnavigation.CelNavScreens.CELESTIAL_BODY_SCREEN
import com.jeffrwatts.celestialnavigation.CelNavScreens.SIGHTS_SCREEN
import com.jeffrwatts.celestialnavigation.CelNavScreens.PLOT_SCREEN
import com.jeffrwatts.celestialnavigation.data.CelestialBody

/**
 * Screens used in [CelNavDestinations]
 */
private object CelNavScreens {
    const val SIGHTS_SCREEN = "sights"
    const val PLOT_SCREEN = "plot"
    const val SIGHT_DETAIL_SCREEN = "sight"
    const val ADD_EDIT_SIGHT_SCREEN = "addEditSight"
    const val CELESTIAL_BODY_SCREEN = "celestialBody"
}

/**
 * Arguments used in [CelNavDestinations] routes
 */
object CelNavDestinationsArgs {
    const val USER_MESSAGE_ARG = "userMessage"
    const val SIGHT_ID_ARG = "sightId"
    const val CELESTIAL_BODY_ARG = "celestialBody"
}

/**
 * Destinations used in the [TasksActivity]
 */
object CelNavDestinations {
    const val SIGHTS_ROUTE = "$SIGHTS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
    const val PLOT_ROUTE = PLOT_SCREEN
    const val ADD_EDIT_SIGHT_ROUTE = "$ADD_EDIT_SIGHT_SCREEN/{$CELESTIAL_BODY_ARG}"
    const val CELESTIAL_BODY_ROUTE = CELESTIAL_BODY_SCREEN
}

/**
 * Models the navigation actions in the app.
 */
class AppNavigationActions(private val navController: NavHostController) {

    fun navigateToPlot() {
        navController.navigate(PLOT_SCREEN) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
        }
    }


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

    fun navigateToAddEditSight(celestialBody: CelestialBody) {
        navController.navigate(
            "$ADD_EDIT_SIGHT_SCREEN/${celestialBody.name}"
        )
    }

    fun navigateToCelestialBodies() {
        navController.navigate(CELESTIAL_BODY_ROUTE)
    }
}