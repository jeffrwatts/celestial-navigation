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
    val filteringUiInfo: FilteringUiInfo = FilteringUiInfo(),
    val userMessage: Int? = null
)

@HiltViewModel
class PlotViewModel @Inject constructor(
    private val sightsRepository: SightsRepository
) : ViewModel() {

    val uiState: StateFlow<PlotUiState> =
        sightsRepository.getSightsStream()
            .map { Async.Success(it) }
            .onStart<Async<Result<List<Sight>>>> { emit(Async.Loading) }
            .map { sightsAsync -> produceLineOfPosition(sightsAsync) }
            .stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = PlotUiState(isLoading = true)
            )

    fun clearAllSights () {
        viewModelScope.launch {
            sightsRepository.deleteAllSights()
        }
    }

    private fun produceLineOfPosition(sightsLoad: Async<Result<List<Sight>>>) =
        when (sightsLoad) {
            Async.Loading -> {
                PlotUiState(isLoading = true)
            }
            is Async.Success -> {
                when (val result = sightsLoad.data) {
                    is Result.Success -> {
                        val lopFromSights = ArrayList<SightLineOfPosition>()

                        for (sight in result.data) {
                            lopFromSights.add(SightLineOfPosition(sight))
                        }
                        PlotUiState(
                            items = lopFromSights,
                            isLoading = false
                        )
                    }
                    else -> PlotUiState(isLoading = false)
                }
            }
        }
}