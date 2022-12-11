package com.jeffrwatts.celestialnavigation.addeditsight

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeffrwatts.celestialnavigation.data.CelestialBody
import com.jeffrwatts.celestialnavigation.data.source.CelestialBodyRepository
import com.jeffrwatts.celestialnavigation.utils.Async
import dagger.hilt.android.lifecycle.HiltViewModel
import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.utils.WhileUiSubscribed
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CelestialBodyUiState(
    val items: List<CelestialBody> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class CelestialBodyViewModel @Inject constructor (
    private val celestialBodyRepository: CelestialBodyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _refreshing = MutableStateFlow(false)
    private val _celestialBodiesAsync = celestialBodyRepository.getCelestialBodiesStream()
        .map { Async.Success(if (it is Result.Success) it.data else emptyList()) }
        .onStart<Async<List<CelestialBody>>> { emit((Async.Loading)) }

    val uiState: StateFlow<CelestialBodyUiState> = combine(_isLoading, _refreshing, _celestialBodiesAsync) {
        isLoading, refreshing, celestialBodyAsync ->
        when(celestialBodyAsync) {
            Async.Loading -> {
                CelestialBodyUiState(isLoading = true, isRefreshing = refreshing)
            }
            is Async.Success -> {
                CelestialBodyUiState(items = celestialBodyAsync.data, isLoading = isLoading, isRefreshing = refreshing)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = WhileUiSubscribed,
        initialValue = CelestialBodyUiState(isLoading = true, isRefreshing = false)
    )

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            celestialBodyRepository.getCelestialBodies(true)
            _refreshing.value = false
        }
    }
}