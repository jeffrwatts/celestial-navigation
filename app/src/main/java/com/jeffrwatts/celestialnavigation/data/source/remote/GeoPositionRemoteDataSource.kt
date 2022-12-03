package com.jeffrwatts.celestialnavigation.data.source.remote

import com.jeffrwatts.celestialnavigation.data.GeoPosition
import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.data.source.GeoPositionDataSource
import com.jeffrwatts.celestialnavigation.network.GeoPositionApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeoPositionRemoteDataSource internal constructor(
    private val geoPositionApi: GeoPositionApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO )
    : GeoPositionDataSource {

    override suspend fun getGeoPosition (body: String, utc: Double): Result<GeoPosition> {
        return withContext(ioDispatcher) {
            try {
                Result.Success(geoPositionApi.getGeographicalPosition(body, utc))
            } catch (exception: Exception) {
                Result.Error(exception)
            }
        }
    }
}