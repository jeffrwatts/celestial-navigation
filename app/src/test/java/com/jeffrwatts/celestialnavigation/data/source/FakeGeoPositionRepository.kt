package com.jeffrwatts.celestialnavigation.data.source

import com.jeffrwatts.celestialnavigation.AddSightTestData
import com.jeffrwatts.celestialnavigation.data.GeoPosition
import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils
import java.lang.Exception

class FakeGeoPositionRepository (testData: List<AddSightTestData>) : GeoPositionRepository {
    private val testDataMap = testData.map { it.test to it }.toMap()

    override suspend fun getGeoPosition(body: String, utc: Double): Result<GeoPosition> {
        testDataMap.get(body)?.let {
            return Result.Success(GeoPosition(it.utc, it.body, it.GHA, it.dec, it.distance))
        }
        return Result.Error(Exception())
    }
}