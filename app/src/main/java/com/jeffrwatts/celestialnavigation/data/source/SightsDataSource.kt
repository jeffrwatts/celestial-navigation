package com.jeffrwatts.celestialnavigation.data.source

import com.jeffrwatts.celestialnavigation.data.Sight
import com.jeffrwatts.celestialnavigation.data.Result
import kotlinx.coroutines.flow.Flow

interface SightsDataSource {
    fun getSightsStream(): Flow<Result<List<Sight>>>

    fun getSightStream(sightId: String): Flow<Result<Sight>>

    suspend fun getSights(): Result<List<Sight>>

    suspend fun getSight(sightId: String): Result<Sight>

    suspend fun saveSight(sight: Sight)

    suspend fun deleteAllSights()

    suspend fun deleteSight(sightId: String)
}