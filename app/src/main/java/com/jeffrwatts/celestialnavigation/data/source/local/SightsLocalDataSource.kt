package com.jeffrwatts.celestialnavigation.data.source.local

import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.data.Sight
import com.jeffrwatts.celestialnavigation.data.source.SightsDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SightsLocalDataSource internal constructor(
    private val sightsDao: SightsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : SightsDataSource {

    override fun getSightsStream(): Flow<Result<List<Sight>>> {
        return sightsDao.observeSights().map {
            Result.Success(it)
        }
    }

    override fun getSightStream(sightId: String): Flow<Result<Sight>> {
        return sightsDao.observeSightById(sightId).map {
            Result.Success(it)
        }
    }

    override suspend fun getSights(): Result<List<Sight>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(sightsDao.getSights())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getSight(sightId: String): Result<Sight> = withContext(ioDispatcher) {
        try {
            val sight = sightsDao.getSightById(sightId)
            if (sight != null) {
                return@withContext Result.Success(sight)
            } else {
                return@withContext Result.Error(Exception("Task not found!"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }


    override suspend fun saveSight(sight: Sight) {
        sightsDao.insertSight(sight)
    }

    override suspend fun activateSight(sight: Sight, activate: Boolean) {
        sightsDao.updateActivated(sight.id, activate)
    }

    override suspend fun deleteAllSights() {
        sightsDao.deleteSights()
    }

    override suspend fun deleteSight(sightId: String) {
        sightsDao.deleteSightById(sightId)
    }
}