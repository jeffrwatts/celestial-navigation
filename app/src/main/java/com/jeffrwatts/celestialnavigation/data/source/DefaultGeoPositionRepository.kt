package com.jeffrwatts.celestialnavigation.data.source

import com.jeffrwatts.celestialnavigation.data.GeoPosition
import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.data.source.remote.GeoPositionRemoteDataSource
import com.jeffrwatts.celestialnavigation.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultGeoPositionRepository(
    private val geoPositionRemoteDataSource: GeoPositionDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GeoPositionRepository {
    override suspend fun getGeoPosition(body: String, utc: Double): Result<GeoPosition> =
        withContext(ioDispatcher) {
            geoPositionRemoteDataSource.getGeoPosition(body, utc)
        }
}