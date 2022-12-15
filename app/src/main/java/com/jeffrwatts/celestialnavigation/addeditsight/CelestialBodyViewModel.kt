package com.jeffrwatts.celestialnavigation.addeditsight


import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeffrwatts.celestialnavigation.data.CelestialBody
import com.jeffrwatts.celestialnavigation.data.source.CelestialBodyRepository
import com.jeffrwatts.celestialnavigation.utils.Async
import dagger.hilt.android.lifecycle.HiltViewModel
import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.data.RiseSet
import com.jeffrwatts.celestialnavigation.data.RiseSetType
import com.jeffrwatts.celestialnavigation.data.source.SightPrefsRepository
import com.jeffrwatts.celestialnavigation.utils.WhileUiSubscribed
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


enum class CelestialBodyVisibilty {
    AlwaysAboveHorizon,
    AboveHorizon,
    BelowHorizon,
    AlwaysBelowHorizon,
    OutOfDate
}

data class CelestialBodyUiState(
    val items: List<CelestialBody> = emptyList(),
    val itemsVisibility: List<CelestialBodyVisibilty> = emptyList(),
    val currentLat: Double = 0.0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class CelestialBodyViewModel @Inject constructor (
    private val celestialBodyRepository: CelestialBodyRepository,
    private val sightPrefsRepository: SightPrefsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _lat = sightPrefsRepository.getAssumedPosition().latitude
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
                val visibility = ArrayList<CelestialBodyVisibilty>()
                celestialBodyAsync.data.forEachIndexed{ index, celestialBody ->
                    visibility.add(getCelestialBodyVisibility(celestialBody))
                }
                CelestialBodyUiState(items = celestialBodyAsync.data,
                    itemsVisibility = visibility,
                    isLoading = isLoading,
                    isRefreshing = refreshing)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = WhileUiSubscribed,
        initialValue = CelestialBodyUiState(isLoading = true, isRefreshing = false)
    )

    private fun getCelestialBodyVisibility(celestialBody: CelestialBody): CelestialBodyVisibilty {
        // If there are no Rise or Set, then the object is permanently above or below based up declination of the object
        // and the observer's latitude.
        if (celestialBody.riseset.isEmpty()) {
            return if (((_lat <= 0.0) and (celestialBody.dec <=0.0)) or
                ((_lat >=0.0) and (celestialBody.dec >= 0.0))) {
                CelestialBodyVisibilty.AlwaysAboveHorizon
            } else {
                CelestialBodyVisibilty.AlwaysBelowHorizon
            }
        }

        val currentTime = System.currentTimeMillis() / 1000.0

        // If the current time is beyond the last rise set time, indicate a refresh is necessary.
        if (currentTime >= celestialBody.riseset[celestialBody.riseset.size-1].utc) {
            return CelestialBodyVisibilty.OutOfDate
        }

        var found = false
        var visibilty = CelestialBodyVisibilty.OutOfDate
        // Walk through and look for the next event.  If the next event is rise, then object is currently
        // below.  If the next event is set, then object is currently above.
        val itr: ListIterator<RiseSet> = celestialBody.riseset.listIterator()

        while (!found and itr.hasNext()) {
            val riseSet = itr.next()
            if (currentTime < riseSet.utc) {
                visibilty = if (riseSet.riseset == RiseSetType.Rise) CelestialBodyVisibilty.BelowHorizon else CelestialBodyVisibilty.AboveHorizon
                found = true
            }
        }
        return visibilty
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            celestialBodyRepository.getCelestialBodies(true)
            _refreshing.value = false
        }
    }
}