package com.jeffrwatts.celestialnavigation.data.source

import com.jeffrwatts.celestialnavigation.data.CelestialBody
import com.jeffrwatts.celestialnavigation.data.Result
import kotlinx.coroutines.flow.Flow

interface CelestialBodyRepository {
    fun getCelestialBodiesStream(): Flow<Result<List<CelestialBody>>>
    suspend fun getCelestialBodies(forceUpdate: Boolean = false): Result<List<CelestialBody>>
}