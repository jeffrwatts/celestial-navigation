package com.jeffrwatts.celestialnavigation.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jeffrwatts.celestialnavigation.data.CelestialBody

@Database(entities = [CelestialBody::class], version = 1, exportSchema = false)
abstract class CelestialBodyDatabase : RoomDatabase() {

    abstract fun celestialBodyDao(): CelestialBodyDao
}