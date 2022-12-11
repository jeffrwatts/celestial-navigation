package com.jeffrwatts.celestialnavigation.data.source

import android.util.Log
import com.jeffrwatts.celestialnavigation.data.CelestialBody
import com.jeffrwatts.celestialnavigation.data.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class DefaultCelestialBodyRepository (
    private val celestialBodyLocalDataSource: CelestialBodyDataSource,
    private val celestialBodyRemoteDataSource: CelestialBodyDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CelestialBodyRepository {
    override fun getCelestialBodiesStream(): Flow<Result<List<CelestialBody>>> {
        return celestialBodyLocalDataSource.getCelestialBodiesStream()
    }

    override suspend fun getCelestialBodies(forceUpdate: Boolean): Result<List<CelestialBody>> {
        Log.d("DefaultCelestialBodyRepository", "START getCelestialBodies: forceUpdate=:$forceUpdate")
        if (forceUpdate) {
            try {
                updateBodiesFromRemoteDataSource()
            } catch (ex: Exception) {
                return Result.Error(ex)
            }
        }
        Log.d("DefaultCelestialBodyRepository", "RETURN getCelestialBodies: forceUpdate=:$forceUpdate")
        return celestialBodyLocalDataSource.getCelestialBodies()
    }

    private suspend fun updateBodiesFromRemoteDataSource() {
        val remoteBodies= celestialBodyRemoteDataSource.getCelestialBodies()

        if (remoteBodies is Result.Success) {
            // Real apps might want to do a proper sync, deleting, modifying or adding each task.
            celestialBodyLocalDataSource.deleteAllCelestialBodies()
            remoteBodies.data.forEach { body ->
                celestialBodyLocalDataSource.saveCelestialBody(body)
            }
        } else if (remoteBodies is Result.Error) {
            throw remoteBodies.exception
        }
    }
}