package addsight

import androidx.lifecycle.SavedStateHandle
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs
import com.jeffrwatts.celestialnavigation.MainCoroutineRule
import com.jeffrwatts.celestialnavigation.addsight.AddSightViewModel
import com.jeffrwatts.celestialnavigation.data.source.*
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

class AddSightViewModelTest {


    private lateinit var addSightViewModel: AddSightViewModel

    // Dependencies
    private lateinit var geoPositionRepository: GeoPositionRepository
    private lateinit var sightPrefsRepository: SightPrefsRepository
    private lateinit var sightsRepository: SightsRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        geoPositionRepository = FakeGeoPositionRepository()
        sightsRepository = FakeSightsRepository()
        sightPrefsRepository = FakeSightPrefsRepository()

    }

    @Test
    fun testSunSights() {
        addSightViewModel = AddSightViewModel(
            sightsRepository,
            geoPositionRepository,
            sightPrefsRepository,
            SavedStateHandle(mapOf(CelNavDestinationsArgs.CELESTIAL_BODY_ARG to "Sun"))
        )

        addSightViewModel.setIc(0.0)
        addSightViewModel.setEyeHeight(10)
        addSightViewModel.setLat(CelNavUtils.angle(19, 0.0, CelNavUtils.North))
        addSightViewModel.setLon(CelNavUtils.angle(156, 0.0, CelNavUtils.West))
        addSightViewModel.setCelestialBody("Sun-LL-Test1")
        addSightViewModel.setHs(CelNavUtils.angle(43, 40.0))
        addSightViewModel.setLimb(CelNavUtils.Limb.Upper)
        addSightViewModel.getGeoPosition()
        addSightViewModel.uiState.value

        assert(valuesEquivalent(addSightViewModel.uiState.value.Ho, CelNavUtils.angle(43, 19.8)))
        assert(valuesEquivalent(addSightViewModel.uiState.value.Hc, CelNavUtils.angle(43, 14.87)))
        assert(valuesEquivalent(addSightViewModel.uiState.value.Zn, CelNavUtils.angle(154, 14.1)))
        assert(valuesEquivalent(addSightViewModel.uiState.value.intercept, 4.88))
    }

    private fun valuesEquivalent(angle1: Double, angle2: Double): Boolean {
        val delta = abs(angle1 - angle2)
        return (delta < 0.001)
    }
}