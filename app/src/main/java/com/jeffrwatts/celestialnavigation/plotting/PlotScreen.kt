package com.jeffrwatts.celestialnavigation.plotting

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.jeffrwatts.celestialnavigation.PlotTopAppBar
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils.konaLat
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils.konaLon


@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun PlotScreen(
    onAddSight: () -> Unit,
    onEditSights: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlotViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            PlotTopAppBar(
                onEditSights = { onEditSights() },
                onClearSights = viewModel::clearAllSights,
                onLoadDB = viewModel::loadDB)
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
        val handleLongClick = remember { mutableStateOf(false) }
        val promptForPermission = remember { mutableStateOf(true) }
        var longClickLatLng = LatLng (0.0, 0.0)

        // If we have a sight, center on one of the first of the assumed positions
        val cameraPosition = LatLng(konaLat, konaLon) // Default to Kona.

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(cameraPosition, 10f)
        }

        val locationPermissionsState = rememberMultiplePermissionsState(
            listOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
        val permissionsGranted = locationPermissionsState.allPermissionsGranted

        if (!permissionsGranted and promptForPermission.value) {
            AlertDialog(
                onDismissRequest = { promptForPermission.value = false },
                title = { Text(text = "Enable Location Permission")},
                text = { Text(text = "To view your actual location, please grant permission")},
                confirmButton = { Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                    Text(text = "Yes")
                }},
                dismissButton = { Button(onClick = { promptForPermission.value = false }) {
                    Text( text = "No")
                }}
            )
        }

        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            cameraPositionState = cameraPositionState,
            onMapLongClick = { longClickLatLng = it; handleLongClick.value = true},
            properties = MapProperties(isMyLocationEnabled = permissionsGranted)
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

            if (handleLongClick.value) {
                Dialog(onDismissRequest = {handleLongClick.value = false })
                {
                    Column(modifier = Modifier
                        .background(
                            color = colorResource(id = R.color.white),
                            shape = RectangleShape
                        )
                        .padding(all = 20.dp)) {
                        Text(text = "Set Position as...")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(onClick = { viewModel.setAssumedPosition(longClickLatLng); handleLongClick.value = false }) {
                                Text(text = "Assumed")
                            }
                            Button(onClick = { viewModel.setFix(longClickLatLng); handleLongClick.value = false }) {
                                Text(text = "Fix")
                            }
                        }
                    }
                }
            }


            uiState.assumedPosition?.let { assumedPosition->
                Marker(
                    state = MarkerState(position = assumedPosition) ,
                    title = "AP: ${displayLatLng(assumedPosition)}"
                )
            }

            uiState.fix?.let { fix->
                Marker(
                    state = MarkerState(position = fix) ,
                    title = "FIX: ${displayLatLng(fix)}"
                )
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