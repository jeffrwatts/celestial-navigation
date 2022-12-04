package com.jeffrwatts.celestialnavigation.addeditsight

import android.icu.text.SimpleDateFormat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.math.*
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs
import com.jeffrwatts.celestialnavigation.R
import com.jeffrwatts.celestialnavigation.data.Result.Success
import com.jeffrwatts.celestialnavigation.data.Sight
import com.jeffrwatts.celestialnavigation.data.source.GeoPositionRepository
import com.jeffrwatts.celestialnavigation.data.source.SightsRepository
import com.jeffrwatts.celestialnavigation.data.succeeded
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class AddEditSightUiState(
    // Sight values
    val celestialBody: String = "",
    val utc: String = "",
    val distance: Double = 0.0,
    val equitorialRadius: Double = 0.0,
    val gha: Double = 0.0,
    val dec: Double = 0.0,
    val hasGP: Boolean = false,

    // Assumed or DR position
    val lat: Double = 0.0,
    val hasLat: Boolean = false,

    val lon: Double = 0.0,
    val hasLon: Boolean = false,

    // Sextant Values
    val Hs: Double = 0.0,
    //val hasHs: Boolean = false,
    val ic: Double = 0.0,
    val eyeHeight: Int = 0,
    val limb: CelNavUtils.Limb = CelNavUtils.Limb.Center,
    val dip: Double = 0.0,
    val refraction: Double = 0.0,
    val SD: Double = 0.0,
    val HP: Double = 0.0,
    val Ho: Double = 0.0,

    // Sight Reduction Values
    val lha: Double = 0.0,
    val Hc: Double = 0.0,
    val Zn: Double = 0.0,
    val lopDirection: CelNavUtils.LOPDirection = CelNavUtils.LOPDirection.Towards,
    val intercept: Double = 0.0,

    // Data State Values
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isSightSaved: Boolean = false
)

@HiltViewModel
class AddEditSightViewModel @Inject constructor (
    private val sightsRepository: SightsRepository,
    private val geoPositionRepository: GeoPositionRepository,
    savedStateHandle: SavedStateHandle) : ViewModel() {

    private val sightId: String? = savedStateHandle[CelNavDestinationsArgs.SIGHT_ID_ARG]

    // A MutableStateFlow needs to be created in this ViewModel. The source of truth of the current
    // editable Task is the ViewModel, we need to mutate the UI state directly in methods such as
    // `updateTitle` or `updateDescription`
    private val _uiState = MutableStateFlow(AddEditSightUiState())
    val uiState: StateFlow<AddEditSightUiState> = _uiState.asStateFlow()

    init {
        if (sightId != null) {
            loadSight(sightId)
        }

        // TODO Read from Prefs for AP, IC, and EyeHeight.
        setIc(-1.0)
        setEyeHeight(16)
        setLat(CelNavUtils.konaLat)
        setLon(CelNavUtils.konaLon)
    }

    fun getGeoPosition(celestialBody: String) {
        val currentTime = System.currentTimeMillis().toDouble() / 1000.0
        viewModelScope.launch {
            when (val geoPositionResult = geoPositionRepository.getGeoPosition(celestialBody, currentTime)) {
                is Success -> {
                    val equitorialRadius = when(geoPositionResult.data.body) {
                        "Sun" -> CelNavUtils.equatorialRadiusSun
                        "Moon" -> CelNavUtils.equatorialRadiusMoon
                        else -> 0.0
                    }
                    _uiState.update {
                        it.copy(
                            celestialBody = geoPositionResult.data.body,
                            utc = geoPositionResult.data.utc,
                            gha = geoPositionResult.data.GHA,
                            dec = geoPositionResult.data.dec,
                            distance = geoPositionResult.data.distance,
                            equitorialRadius = equitorialRadius,
                            hasGP = true)
                    }
                    doSightReduction()
                }
                else -> {
                    _uiState.update {
                        it.copy(userMessage = R.string.failed_to_get_position)
                    }
                }
            }
        }
    }

    fun setLat (lat: Double) {
        _uiState.update {
            it.copy(lat = lat, hasLat = true)
        }
        doSightReduction()
    }

    fun setLon (lon: Double) {
        _uiState.update {
            it.copy(lon = lon, hasLon = true)
        }
        doSightReduction()
    }

    fun setHs (Hs: Double) {
        _uiState.update {
            it.copy(Hs = Hs)
        }
        doSightReduction()
    }

    fun setIc (ic: Double) {
        _uiState.update {
            it.copy(ic = ic)
        }
        doSightReduction()
    }

    fun setEyeHeight (eyeHeight: Int) {
        val dip = CelNavUtils.calculateDipCorrection(eyeHeight)
        _uiState.update {
            it.copy(eyeHeight = eyeHeight, dip = dip)
        }
        doSightReduction()
    }

    fun setLimb (limb: CelNavUtils.Limb) {
        _uiState.update {
            it.copy(limb = limb)
        }
        doSightReduction()
    }

    fun saveSight() {
        if (!canDoSightReduction()) {
            _uiState.update {
                it.copy(userMessage = R.string.empty_sight)
            }
            return
        }

        if (sightId == null) {
            createNewSight()
        } else {
            updateSight()
        }
    }

    fun snackbarMessageShown() {
        _uiState.update {
            it.copy(userMessage = null)
        }
    }

    private fun canDoSightReduction(): Boolean {
        return ((uiState.value.hasLat) and (uiState.value.hasLon) and (uiState.value.hasGP))
    }

    private fun doSightReduction() {
        if (!canDoSightReduction()) return

        var Ha = uiState.value.Hs + (uiState.value.ic + uiState.value.dip) / 60.0

        val refraction = CelNavUtils.calculateRefractionCorrection(Ha)
        Ha += refraction / 60.0

        val sd = CelNavUtils.calculateSemiDiameterCorrection(Ha, uiState.value.distance, uiState.value.equitorialRadius, uiState.value.limb)
        val hp = CelNavUtils.calculateHorizontalParallaxCorrection(Ha, uiState.value.distance, uiState.value.lat, uiState.value.celestialBody == "Moon")

        val Ho = Ha + (sd + hp) / 60.0

        // Calculate Local Hour Angle
        val localHourAngle = CelNavUtils.calculateLocalHourAngle(uiState.value.gha, uiState.value.lon)

        // Do Sight Reduction
        val (Hc, _, Zn) = CelNavUtils.calculateSightReduction(uiState.value.dec, uiState.value.lat, localHourAngle)

        val intercept = CelNavUtils.roundToPrecision(abs(Ho - Hc)*60.0, 2)
        val direction = if (Hc < Ho) CelNavUtils.LOPDirection.Towards else CelNavUtils.LOPDirection.Away

        _uiState.update {
            it.copy(
                refraction = refraction,
                SD = sd,
                HP = hp,
                Ho = Ho,
                lha = localHourAngle,
                Hc = Hc,
                Zn = Zn,
                lopDirection = direction,
                intercept = intercept,
            )
        }
    }

    private fun createNewSight() = viewModelScope.launch {
        val newSight = Sight(
            celestialBody = uiState.value.celestialBody,
            utc = uiState.value.utc,
            distance = uiState.value.distance,
            equitorialRadius = uiState.value.equitorialRadius,
            gha = uiState.value.gha,
            dec = uiState.value.dec,
            lat = uiState.value.lat,
            lon = uiState.value.lon,
            Hs = uiState.value.Hs,
            ic = uiState.value.ic,
            eyeHeight = uiState.value.eyeHeight,
            limb = uiState.value.limb,
            dip = uiState.value.dip,
            refraction = uiState.value.refraction,
            SD = uiState.value.SD,
            HP = uiState.value.HP,
            Ho = uiState.value.Ho,
            lha = uiState.value.lha,
            Hc = uiState.value.Hc,
            Zn = uiState.value.Zn,
            lopDirection = uiState.value.lopDirection,
            intercept = uiState.value.intercept,
            isActive = true)
        sightsRepository.saveSight(newSight)
        _uiState.update {
            it.copy(isSightSaved = true)
        }
    }

    private fun updateSight() {
        if (sightId == null) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        viewModelScope.launch {
            val updatedSight = Sight(
                celestialBody = uiState.value.celestialBody,
                utc = uiState.value.utc,
                distance = uiState.value.distance,
                equitorialRadius = uiState.value.equitorialRadius,
                gha = uiState.value.gha,
                dec = uiState.value.dec,
                lat = uiState.value.lat,
                lon = uiState.value.lon,
                Hs = uiState.value.Hs,
                ic = uiState.value.ic,
                eyeHeight = uiState.value.eyeHeight,
                limb = uiState.value.limb,
                dip = uiState.value.dip,
                refraction = uiState.value.refraction,
                SD = uiState.value.SD,
                HP = uiState.value.HP,
                Ho = uiState.value.Ho,
                lha = uiState.value.lha,
                Hc = uiState.value.Hc,
                Zn = uiState.value.Zn,
                lopDirection = uiState.value.lopDirection,
                intercept = uiState.value.intercept)
            sightsRepository.saveSight(updatedSight)
            _uiState.update {
                it.copy(isSightSaved = true)
            }
        }
    }

    private fun loadSight(sightId: String) {
        _uiState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            sightsRepository.getSight(sightId).let { result ->
                if (result is Success) {
                    val sight = result.data
                    _uiState.update {
                        it.copy(
                            celestialBody = sight.celestialBody,
                            utc = sight.utc,
                            distance = sight.distance,
                            equitorialRadius = sight.equitorialRadius,
                            gha = sight.gha,
                            dec = sight.dec,
                            lat = sight.lat,
                            lon = sight.lon,
                            Hs = sight.Hs,
                            ic = sight.ic,
                            limb = sight.limb,
                            eyeHeight = sight.eyeHeight,
                            dip = sight.dip,
                            refraction = sight.refraction,
                            SD = sight.SD,
                            HP = sight.HP,
                            Ho = sight.Ho,
                            lha = sight.lha,
                            Hc = sight.Hc,
                            Zn = sight.Zn,
                            lopDirection = sight.lopDirection,
                            intercept = sight.intercept,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                }
            }
        }
    }
}