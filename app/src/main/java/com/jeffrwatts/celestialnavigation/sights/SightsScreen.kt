package com.jeffrwatts.celestialnavigation.sights

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeffrwatts.celestialnavigation.data.Sight
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.SightsTopAppBar
import com.jeffrwatts.celestialnavigation.ui.theme.Typography
import com.jeffrwatts.celestialnavigation.utils.LoadingContent

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SightsScreen(
    @StringRes userMessage: Int,
    onAddSight: () -> Unit,
    onSightClick: (Sight) -> Unit,
    onUserMessageDisplayed: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SightsViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            SightsTopAppBar(
                onBack = onBack
            )
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

        SightsContent(
            loading = uiState.isLoading,
            sights = uiState.items,
            currentFilteringLabel = uiState.filteringUiInfo.currentFilteringLabel,
            noSightsLabel = uiState.filteringUiInfo.noTasksLabel,
            noSightsIconRes = uiState.filteringUiInfo.noTaskIconRes,
            onSightClick = onSightClick,
            onSightActivatedChange = viewModel::activateSight,
            modifier = Modifier.padding(paddingValues)
        )

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

@Composable
private fun SightsContent(
    loading: Boolean,
    sights: List<Sight>,
    @StringRes currentFilteringLabel: Int,
    @StringRes noSightsLabel: Int,
    @DrawableRes noSightsIconRes: Int,
    onSightClick: (Sight) -> Unit,
    onSightActivatedChange: (Sight, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LoadingContent(
        loading = loading,
        empty = sights.isEmpty() && !loading,
        emptyContent = { SightsEmptyContent(noSightsLabel, noSightsIconRes, modifier) },
        onRefresh = {}
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
            Column {
                for (sight in sights)
                    SightItem(
                        sight = sight,
                        onTaskClick = onSightClick,
                        onCheckedChange = { onSightActivatedChange(sight, it) }
                    )
                }
            }
        }
    }


@Composable
private fun SightItem(
    sight: Sight,
    onCheckedChange: (Boolean) -> Unit,
    onTaskClick: (Sight) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.horizontal_margin),
                vertical = dimensionResource(id = R.dimen.list_item_padding),
            )
            .clickable { onTaskClick(sight) }
    ) {
        Checkbox(
            checked = sight.isActive,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = "${sight.celestialBody} at ${sight.utc}",
            style = Typography.bodyLarge,
            modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.horizontal_margin)
            )
        )
    }
}

@Composable
private fun SightsEmptyContent(
    @StringRes noTasksLabel: Int,
    @DrawableRes noTasksIconRes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = noTasksIconRes),
            contentDescription = stringResource(R.string.no_sights_image_content_description),
            modifier = Modifier.size(96.dp)
        )
        Text(stringResource(id = noTasksLabel))
    }
}
