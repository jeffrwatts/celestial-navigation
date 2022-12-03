package com.jeffrwatts.celestialnavigation.addeditsight

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import com.jeffrwatts.celestialnavigation.AddEditSightTopAppBar
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.utils.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun AddEditSightScreen(
    @StringRes topBarTitle: Int,
    onSightUpdate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditSightViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AddEditSightTopAppBar(topBarTitle, onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::saveSight) {
                Icon(Icons.Filled.Done, stringResource(id = R.string.save_sight))
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        AddEditSightContent(
            uiState,
            onGetGeographicalPosition = { celestialBody->
                viewModel.getGeoPosition(celestialBody)

                // TODO Remove.
                viewModel.setIc(1.0)
                viewModel.setEyeHeight(10)
                viewModel.setLat(CelNavUtils.konaLat)
                viewModel.setLon(CelNavUtils.konaLon)
            },
            onHsChanged = viewModel::setHs,
            onIcChanged = viewModel::setIc,
            onEyeHeightChanged = viewModel::setEyeHeight,
            onLatChanged = viewModel::setLat,
            onLonChanged = viewModel::setLon,
            modifier = Modifier.padding(paddingValues)
        )

        // Check if the task is saved and call onTaskUpdate event
        LaunchedEffect(uiState.isSightSaved) {
            if (uiState.isSightSaved) {
                onSightUpdate()
            }
        }

        // Check for user messages to display on the screen
        uiState.userMessage?.let { userMessage ->
            val snackbarText = stringResource(userMessage)
            LaunchedEffect(viewModel, userMessage, snackbarText) {
                snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }
    }
}

@Composable
private fun AddEditSightContent(
    uiState: AddEditSightUiState,
    onGetGeographicalPosition: (celestialBody: String)->Unit,
    onHsChanged: (newHs: Double)->Unit,
    onIcChanged: (newIc: Double)->Unit,
    onEyeHeightChanged: (newEyeHeight: Int)->Unit,
    onLatChanged: (newLat: Double)->Unit,
    onLonChanged: (newLon: Double)->Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        //SwipeRefresh(
        //    // Show the loading spinner—`loading` is `true` in this code path
        //    state = rememberSwipeRefreshState(true),
        //    onRefresh = { /* DO NOTHING */ },
        //    content = { },
        //)
    } else {
        Column(
            modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(id = R.dimen.horizontal_margin))
                .verticalScroll(rememberScrollState())
        ) {
            CelestialBodyDropDown ( onGetGeographicalPosition )
            UTC(uiState.utc)
            AngleDisplay(label = "GHA:", angle = uiState.gha)
            Dec(angle = uiState.dec)
            Divider(thickness = 2.dp)
            AngleInput(label = "Hs:", angle = uiState.Hs, onHsChanged)
            IC(uiState.ic, onIcChanged)
            Dip(uiState.eyeHeight, uiState.dip, onEyeHeightChanged)
            MinutesValue(label = "Refr:", minutes = uiState.refraction)
            MinutesValue(label = "SD:", minutes = uiState.SD)
            MinutesValue(label = "HP:", minutes = uiState.HP)
            AngleDisplay(label = "Ho:", angle = uiState.Ho)
            Divider(thickness = 2.dp)
            AngleInput(label = "Lat:", angle = uiState.lat, onLatChanged, "N", "S")
            AngleInput(label = "Lon:", angle = uiState.lon, onLonChanged, "E", "W")
            Divider(thickness = 2.dp)
            AngleDisplay(label = "LHA:", angle = uiState.lha)
            AngleDisplay(label = "Hc:", uiState.Hc)
            AngleDisplay(label = "Zn:", uiState.Zn)
            Intercept(intercept = uiState.intercept, direction = uiState.lopDirection)
        }
    }
}