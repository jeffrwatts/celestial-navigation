package com.jeffrwatts.celestialnavigation.plotting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.jeffrwatts.celestialnavigation.ADD_EDIT_RESULT_OK
import com.jeffrwatts.celestialnavigation.DELETE_RESULT_OK
import com.jeffrwatts.celestialnavigation.EDIT_RESULT_OK
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.data.Sight
import com.jeffrwatts.celestialnavigation.data.source.SightsRepository
import com.jeffrwatts.celestialnavigation.sights.FilteringUiInfo
import com.jeffrwatts.celestialnavigation.utils.Async
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils
import com.jeffrwatts.celestialnavigation.utils.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.jeffrwatts.celestialnavigation.data.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


data class SightLineOfPosition(val sight: Sight) {
    val assumedPosition: LatLng
    val interceptLatLon: LatLng
    val leftLatLon: LatLng
    val rightLatLon: LatLng

    init {
        assumedPosition = LatLng(sight.lat, sight.lon)
        val correctedZn = CelNavUtils.angleAdd(sight.Zn, 180.0)

        // Calculate the Intercept for the Line of Position
        val (interceptLat, interceptLon) = CelNavUtils.calculateDestination(sight.lat, sight.lon, correctedZn, sight.intercept)
        interceptLatLon = LatLng(interceptLat, interceptLon)

        // Calculate left bearing for display
        val bearingLeft = CelNavUtils.angleAdd(correctedZn,270.0)
        val (leftLat, leftLon) = CelNavUtils.calculateDestination(interceptLat, interceptLon, bearingLeft, 10.0)
        leftLatLon = LatLng(leftLat, leftLon)

        // Calculate right bearing for display
        val bearingRight = CelNavUtils.angleAdd(correctedZn,90.0)
        val (rightLat, rightLon) = CelNavUtils.calculateDestination(interceptLat, interceptLon, bearingRight, 10.0)
        rightLatLon = LatLng(rightLat, rightLon)
    }
}

/**
 * UiState for the plot screen.
 */
data class PlotUiState(
    val items: List<SightLineOfPosition> = emptyList(),
    val isLoading: Boolean = false,
    val assumedPosition: LatLng? = null,
    val fix: LatLng? = null
)

@HiltViewModel
class PlotViewModel @Inject constructor(
    private val sightsRepository: SightsRepository
) : ViewModel() {


    private val _fix: MutableStateFlow<LatLng?> = MutableStateFlow(null)
    private val _assumedPosition: MutableStateFlow<LatLng?> = MutableStateFlow(null)
    private val _sightsAsync = sightsRepository.getSightsStream()
        .map { Async.Success(it) }
        .onStart<Async<Result<List<Sight>>>> { emit(Async.Loading) }

    val uiState: StateFlow<PlotUiState> = combine(_assumedPosition, _fix, _sightsAsync) {
            assumedPosition, fix, sightsAsync ->
        when (sightsAsync) {
            Async.Loading -> {
                PlotUiState(isLoading = true)
            }
            is Async.Success -> {
                when (val result = sightsAsync.data) {
                    is Result.Success -> {
                        val lopFromSights = ArrayList<SightLineOfPosition>()

                        for (sight in result.data) {
                            lopFromSights.add(SightLineOfPosition(sight))
                        }
                        PlotUiState(
                            items = lopFromSights,
                            isLoading = false,
                            assumedPosition = assumedPosition,
                            fix = fix
                        )
                    }
                    else -> PlotUiState(isLoading = false)
                }
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = PlotUiState(isLoading = true)
        )
    fun setFix(fix: LatLng) {
        _fix.value = fix
    }

    fun setAssumedPosition(assumedPosition: LatLng) {
        _assumedPosition.value = assumedPosition
    }

    fun clearAllSights () {
        viewModelScope.launch {
            sightsRepository.deleteAllSights()
        }
    }
}