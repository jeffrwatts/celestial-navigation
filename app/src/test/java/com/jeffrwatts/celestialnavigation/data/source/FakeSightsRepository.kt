package com.jeffrwatts.celestialnavigation.data.source

import com.jeffrwatts.celestialnavigation.data.Result
import com.jeffrwatts.celestialnavigation.data.Sight
import kotlinx.coroutines.flow.Flow

class FakeSightsRepository : SightsRepository {
    override fun getSightsStream(): Flow<Result<List<Sight>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getSights(forceUpdate: Boolean): Result<List<Sight>> {
        TODO("Not yet implemented")
    }

    override fun getSightStream(sightId: String): Flow<Result<Sight>> {
        TODO("Not yet implemented")
    }

    override suspend fun getSight(sightId: String, forceUpdate: Boolean): Result<Sight> {
        TODO("Not yet implemented")
    }

    override suspend fun saveSight(sight: Sight) {
        // No-Op
    }

    override suspend fun activateSight(sight: Sight, activate: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllSights() {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSight(sightId: String) {
        TODO("Not yet implemented")
    }
}