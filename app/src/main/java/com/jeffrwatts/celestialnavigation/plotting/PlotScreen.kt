package com.jeffrwatts.celestialnavigation.plotting

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.jeffrwatts.celestialnavigation.PlotTopAppBar
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils


@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlotScreen(
    @StringRes userMessage: Int,
    onAddSight: () -> Unit,
    onEditSights: () -> Unit,
    onClearSights: () -> Unit,
    onUserMessageDisplayed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlotViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            PlotTopAppBar(
                onEditSights = onEditSights,
                onClearSights = onClearSights)
        },
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSight) {
                Icon(Icons.Filled.Add, stringResource(id = R.string.add_sight))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        // If we have a sight, center on one of the first of the assumed positions
        val cameraPosition = LatLng(19.6419, -155.9962) // Default to Kona.

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(cameraPosition, 10f)
        }

        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            cameraPositionState = cameraPositionState
        )
        {
            if (!uiState.isLoading) {
                val builder = LatLngBounds.Builder()
                uiState.items.forEach { lop->
                    Marker(
                        state = MarkerState(position = lop.assumedPosition),
                        title = "AP: ${displayLatLng(lop.assumedPosition)}"
                    )
                    Marker(
                        state = MarkerState(position = lop.interceptLatLon),
                        title = displayIntercept(lop.sight.intercept, lop.sight.lopDirection, lop.sight.Zn)
                    )
                    Polyline(points = listOf(lop.assumedPosition, lop.interceptLatLon), color = Color(0x3,0xDA,0xC5))
                    Polyline(points = listOf(lop.leftLatLon, lop.interceptLatLon, lop.rightLatLon), color = Color(0x62, 0x00, 0xEE))
                    builder.include(lop.assumedPosition)
                    builder.include(lop.interceptLatLon)
                    builder.include(lop.leftLatLon)
                    builder.include(lop.rightLatLon)
                }

                if (uiState.items.isNotEmpty()) {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(builder.build(), 64))
                }
            }
        }

        // Check for user messages to display on the screen
        uiState.userMessage?.let { message ->
            val snackbarText = stringResource(message)
            LaunchedEffect(viewModel, message, snackbarText) {
                snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        // Check if there's a userMessage to show to the user
        val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
        LaunchedEffect(userMessage) {
            if (userMessage != 0) {
                viewModel.showEditResultMessage(userMessage)
                currentOnUserMessageDisplayed()
            }
        }
    }
}

fun displayAngle (angle: Double, positiveSign: String?=null, negativeSign: String?=null): String {
    val (deg, min, sign) = CelNavUtils.degreesMinutesSign(angle)
    var display = "${deg}° ${min}'"
    if (!positiveSign.isNullOrEmpty() and !negativeSign.isNullOrEmpty()) {
        display += if (sign == 1) "$positiveSign" else "$negativeSign"
    }
    return display
}

fun displayLatLng(latlng: LatLng):String {
    return "${displayAngle(latlng.latitude, "N", "S")}; ${displayAngle(latlng.longitude, "E", "W")}"
}

fun displayIntercept(intercept: Double, lopDirection: CelNavUtils.LOPDirection, Zn: Double): String {
    return "$intercept nm ${if (lopDirection == CelNavUtils.LOPDirection.Towards) "Towards" else "Away"}; Zn = ${displayAngle(Zn)}"
}