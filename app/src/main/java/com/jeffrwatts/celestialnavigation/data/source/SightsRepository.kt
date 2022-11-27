package com.jeffrwatts.celestialnavigation.data.source

import com.jeffrwatts.celestialnavigation.data.Sight
import com.jeffrwatts.celestialnavigation.data.Result
import kotlinx.coroutines.flow.Flow

interface SightsRepository {
    fun getSightsStream(): Flow<Result<List<Sight>>>

    suspend fun getSights(forceUpdate: Boolean = false): Result<List<Sight>>

    fun getSightStream(sightId: String): Flow<Result<Sight>>

    suspend fun getSight(sightId: String, forceUpdate: Boolean = false): Result<Sight>

    suspend fun saveSight(sight: Sight)

    suspend fun activateSight(sight: Sight, activate: Boolean)

    suspend fun deleteAllSights()

    suspend fun deleteSight(sightId: String)
}