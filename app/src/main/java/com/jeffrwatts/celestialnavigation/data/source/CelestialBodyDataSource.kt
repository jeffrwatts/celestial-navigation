package com.jeffrwatts.celestialnavigation.data.source

import com.jeffrwatts.celestialnavigation.data.CelestialBody
import com.jeffrwatts.celestialnavigation.data.Result
import kotlinx.coroutines.flow.Flow

interface CelestialBodyDataSource {
    fun getCelestialBodiesStream(): Flow<Result<List<CelestialBody>>>
    suspend fun getCelestialBodies(): Result<List<CelestialBody>>
    suspend fun saveCelestialBody(body: CelestialBody)
    suspend fun deleteAllCelestialBodies()
}