package com.jeffrwatts.celestialnavigation.data.source.local

import com.jeffrwatts.celestialnavigation.data.CelestialBody
import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.data.source.CelestialBodyDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CelestialBodyLocalDataSource internal constructor(
    private val celestialBodyDao: CelestialBodyDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CelestialBodyDataSource {
    override fun getCelestialBodiesStream(): Flow<Result<List<CelestialBody>>> {
        return celestialBodyDao.observeCelestialBodies().map {
            Result.Success(it)
        }
    }

    override suspend fun getCelestialBodies(): Result<List<CelestialBody>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(celestialBodyDao.getCelestialBodies())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun saveCelestialBody(body: CelestialBody) {
        celestialBodyDao.insertCelestialBody(body)
    }

    override suspend fun deleteAllCelestialBodies() {
        celestialBodyDao.deleteCelestialBodies()
    }
}