package com.jeffrwatts.celestialnavigation.data.source.local

import androidx.room.*
import com.jeffrwatts.celestialnavigation.data.CelestialBody
import kotlinx.coroutines.flow.Flow

@Dao
interface CelestialBodyDao {
    @Query("SELECT * FROM celestialBodies")
    fun observeCelestialBodies(): Flow<List<CelestialBody>>

    @Query("SELECT * FROM celestialBodies")
    suspend fun getCelestialBodies(): List<CelestialBody>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCelestialBody(body: CelestialBody)

    @Update
    suspend fun updateCelestialBody(body: CelestialBody): Int

    @Query("DELETE FROM celestialBodies")
    suspend fun deleteCelestialBodies()
}