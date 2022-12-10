package com.jeffrwatts.celestialnavigation.addeditsight

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeffrwatts.celestialnavigation.CelestialBodyTopAppBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun CelestialBodyScreen(
    onBack:() -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CelestialBodyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { CelestialBodyTopAppBar(onBack) },
        modifier = modifier.fillMaxSize())
    { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(uiState.items) { celestialBody->
                Row() {
                    Text(text = celestialBody.name)
                }
            }
        }
    }
}