package com.jeffrwatts.celestialnavigation.addsight

import android.os.Build
import androidx.lifecycle.SavedStateHandle
import com.jeffrwatts.celestialnavigation.AddSightTestData
import com.jeffrwatts.celestialnavigation.CelNavDestinationsArgs
import com.jeffrwatts.celestialnavigation.MainCoroutineRule
import com.jeffrwatts.celestialnavigation.data.source.*
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import kotlin.math.abs

class AddSightViewModelTest {
    private lateinit var addSightViewModel: AddSightViewModel

    // Test Data
    private var testData: List<AddSightTestData>? = null

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
        testData = loadUnitTestData()
        assert (!testData.isNullOrEmpty())

        geoPositionRepository = FakeGeoPositionRepository(testData!!)
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

        testData?.forEach {
            addSightViewModel.setCelestialBody(it.test)
            addSightViewModel.setHs(it.Hs)
            addSightViewModel.setIc(it.ic)
            addSightViewModel.setEyeHeight(it.eyeHeight)
            addSightViewModel.setLimb(it.limb)
            addSightViewModel.setLat(it.lat)
            addSightViewModel.setLon(it.lon)
            addSightViewModel.getGeoPosition()
            assert(valuesEquivalent(addSightViewModel.uiState.value.Hc, it.Hc))
            assert(valuesEquivalent(addSightViewModel.uiState.value.Zn, it.Zn))
            assert(valuesEquivalent(addSightViewModel.uiState.value.intercept, it.a))
            assert(addSightViewModel.uiState.value.lopDirection == it.direction)
        }
    }

    private fun valuesEquivalent(angle1: Double, angle2: Double): Boolean {
        val delta = abs(angle1 - angle2)
        return (delta < 0.001)
    }

    private fun loadUnitTestData(): List<AddSightTestData>? {
        val inputStream = javaClass.classLoader?.getResourceAsStream("addsighttestdata.json")

        inputStream?.let {
            val testDataString = String(it.readAllBytes(), StandardCharsets.UTF_8)
            val moshi = Moshi.Builder().build()
            val listOfTestDataType: Type = Types.newParameterizedType(MutableList::class.java, AddSightTestData::class.java)
            val jsonAdapter: JsonAdapter<List<AddSightTestData>> = moshi.adapter(listOfTestDataType)
            return jsonAdapter.fromJson(testDataString)
        }
        return emptyList()
    }
}