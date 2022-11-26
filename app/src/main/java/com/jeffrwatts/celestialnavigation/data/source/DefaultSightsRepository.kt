package com.jeffrwatts.celestialnavigation.data.source

import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.data.Sight
import com.jeffrwatts.celestialnavigation.data.source.local.SightsLocalDataSource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

class DefaultSightsRepository (
    // Add a remote DB if desired later.  private val sightsRemoteDataSource: SightsDataSource,
    private val sightsLocalDataSource: SightsDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SightsRepository {

    override fun getSightsStream(): Flow<Result<List<Sight>>> {
        return sightsLocalDataSource.getSightsStream()
    }

    override suspend fun getSights(forceUpdate: Boolean): Result<List<Sight>> {
        //if (forceUpdate) {
        //    try {
        //        updateSightsFromRemoteDataSource()
        //    } catch (ex: Exception) {
        //        return Result.Error(ex)
        //    }
        //}
        return sightsLocalDataSource.getSights()
    }

    override fun getSightStream(sightId: String): Flow<Result<Sight>> {
        return sightsLocalDataSource.getSightStream(sightId)
    }

    override suspend fun getSight(sightId: String, forceUpdate: Boolean): Result<Sight> {
        //if (forceUpdate) {
        //    updateSightFromRemoteDataSource(sightId)
        //}
        return sightsLocalDataSource.getSight(sightId)
    }

    override suspend fun saveSight(sight: Sight) {
        coroutineScope {
            // Save to remote, then to local.
            //launch { sightsRemoteDataSource.saveSight(sight) }
            launch { sightsLocalDataSource.saveSight(sight) }
        }
    }

    override suspend fun deleteAllSights() {
        withContext(ioDispatcher) {
            coroutineScope {
                //launch { sightsRemoteDataSource.deleteAllSights() }
                launch { sightsLocalDataSource.deleteAllSights() }
            }
        }
    }

    override suspend fun deleteSight(sightId: String) {
        coroutineScope {
            //launch { sightsRemoteDataSource.deleteSight(sightId) }
            launch { sightsLocalDataSource.deleteSight(sightId) }
        }
    }

/*
    // Example of how to update from Remote.
    private suspend fun updateSightFromRemoteDataSource(sightId: String) {
        val remoteSight = sightsRemoteDataSource.getSight(sightId)

        if (remoteSight is Result.Success) {
            sightsLocalDataSource.saveSight(remoteSight.data)
        }
    }

    private suspend fun updateSightsFromRemoteDataSource() {
        val remoteSights = sightsRemoteDataSource.getSights()

        if (remoteSights is Result.Success) {
            // Real apps might want to do a proper sync, deleting, modifying or adding each task.
            sightsLocalDataSource.deleteAllSights()
            remoteSights.data.forEach { task ->
                sightsLocalDataSource.saveSight(task)
            }
        } else if (remoteSights is Result.Error) {
            throw remoteSights.exception
        }
    }
 */
}