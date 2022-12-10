package com.jeffrwatts.celestialnavigation.addeditsight

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeffrwatts.celestialnavigation.data.CelestialBody
import com.jeffrwatts.celestialnavigation.data.source.CelestialBodyRepository
import com.jeffrwatts.celestialnavigation.utils.Async
import dagger.hilt.android.lifecycle.HiltViewModel
import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.utils.WhileUiSubscribed
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CelestialBodyUiState(
    val items: List<CelestialBody> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CelestialBodyViewModel @Inject constructor (
    private val celestialBodyRepository: CelestialBodyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _celestialBodiesAsync = celestialBodyRepository.getCelestialBodiesStream()
        .map { Async.Success(if (it is Result.Success) it.data else emptyList()) }
        .onStart<Async<List<CelestialBody>>> { emit((Async.Loading)) }

    val uiState: StateFlow<CelestialBodyUiState> = combine(_isLoading, _celestialBodiesAsync) {
        isLoading, celestialBodyAsync ->
        when(celestialBodyAsync) {
            Async.Loading -> {
                CelestialBodyUiState(isLoading = true)
            }
            is Async.Success -> {
                CelestialBodyUiState(items = celestialBodyAsync.data, isLoading = isLoading)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = WhileUiSubscribed,
        initialValue = CelestialBodyUiState(isLoading = true)
    )
}