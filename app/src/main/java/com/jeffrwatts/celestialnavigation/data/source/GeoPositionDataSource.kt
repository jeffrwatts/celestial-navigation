package com.jeffrwatts.celestialnavigation.data.source

import com.jeffrwatts.celestialnavigation.data.GeoPosition
import com.jeffrwatts.celestialnavigation.data.Result

interface GeoPositionDataSource {
    suspend fun getGeoPosition(body: String, utc: Double): Result<GeoPosition>
}