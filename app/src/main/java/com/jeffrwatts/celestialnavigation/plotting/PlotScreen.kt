package com.jeffrwatts.celestialnavigation.plotting

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeffrwatts.celestialnavigation.PlotTopAppBar
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.SightsTopAppBar
import com.jeffrwatts.celestialnavigation.data.Sight
import com.jeffrwatts.celestialnavigation.sights.SightsFilterType


@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlotScreen(
    onAddSight: () -> Unit,
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlotViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            PlotTopAppBar(
                openDrawer = openDrawer
            )
        },
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSight) {
                Icon(Icons.Filled.Add, stringResource(id = R.string.add_sight))
            }
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        Column(modifier = Modifier.padding(paddingValues)) {
            for (lop in uiState.items) {
                Text(text = "Assumed Position: ${lop.assumedPosition}")
                Text(text = "Intercept: ${lop.interceptLatLon}")
                Text(text = "Left: ${lop.leftLatLon}")
                Text(text = "Right: ${lop.rightLatLon}")
                Divider(thickness = 4.dp)
            }
        }
    }
}
