package com.jeffrwatts.celestialnavigation.data.source

import com.jeffrwatts.celestialnavigation.data.GeoPosition
import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.data.source.remote.GeoPositionRemoteDataSource

interface GeoPositionRepository {
    suspend fun getGeoPosition(body: String, utc: Double): Result<GeoPosition>
}