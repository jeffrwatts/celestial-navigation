package com.jeffrwatts.celestialnavigation.sights

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeffrwatts.celestialnavigation.SightsTopAppBar
import com.jeffrwatts.celestialnavigation.data.Sight
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.ui.theme.Typography

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SightsScreen(
    @StringRes userMessage: Int,
    onAddSight: () -> Unit,
    onSightClick: (Sight) -> Unit,
    onUserMessageDisplayed: () -> Unit,
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SightsViewModel = hiltViewModel(),
    //scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    Scaffold(
        //scaffoldState = scaffoldState,
        topBar = {
            SightsTopAppBar(
                openDrawer = openDrawer,
                onFilterAllSights = { viewModel.setFiltering(SightsFilterType.ALL_SIGHTS) },
                onFilterActiveSights = { /*viewModel.setFiltering(ACTIVE_TASKS)*/ },
                onFilterCompletedSights = { /*viewModel.setFiltering(COMPLETED_TASKS)*/ },
                onClearCompletedSights = { /*viewModel.clearCompletedTasks()*/ },
                onRefresh = { /*viewModel.refresh()*/ }
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

        SightsContent(
            sights = uiState.items,
            currentFilteringLabel = uiState.filteringUiInfo.currentFilteringLabel,
            onSightClick = onSightClick,
            modifier = Modifier.padding(paddingValues)
        )

        // Check for user messages to display on the screen
        //uiState.userMessage?.let { message ->
        //    val snackbarText = stringResource(message)
        //    LaunchedEffect(scaffoldState, viewModel, message, snackbarText) {
        //        scaffoldState.snackbarHostState.showSnackbar(snackbarText)
        //        viewModel.snackbarMessageShown()
        //    }
        //}

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

@Composable
private fun SightsContent(
    sights: List<Sight>,
    @StringRes currentFilteringLabel: Int,
    onSightClick: (Sight) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
    ) {
        Text(
            text = stringResource(currentFilteringLabel),
            modifier = Modifier.padding(
                horizontal = dimensionResource(id = R.dimen.list_item_padding),
                vertical = dimensionResource(id = R.dimen.vertical_margin)
            ),
            style = Typography.bodyLarge
        )

        for (sight in sights) {
            SightItem(sight, onSightClick)
        }
    }
}

@Composable
private fun SightItem(
    sight: Sight,
    onSightClick: (Sight) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.horizontal_margin),
                vertical = dimensionResource(id = R.dimen.list_item_padding),
            )
            .clickable { onSightClick(sight) }
    ) {
        Text(
            text = "${sight.celestialBody} at ${sight.utc}",
            style = Typography.bodyLarge,
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.horizontal_margin)
            )
        )
    }
}
