package com.jeffrwatts.celestialnavigation.data.source

import com.jeffrwatts.celestialnavigation.data.GeoPosition
import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils

class FakeGeoPositionRepository : GeoPositionRepository {

    override suspend fun getGeoPosition(body: String, utc: Double): Result<GeoPosition> {
        return Result.Success(GeoPosition("", "Sun",
            CelNavUtils.angle(135, 49.1),
            CelNavUtils.angle(23, 24.12, CelNavUtils.South),
            147205487.77383572))
    }
}