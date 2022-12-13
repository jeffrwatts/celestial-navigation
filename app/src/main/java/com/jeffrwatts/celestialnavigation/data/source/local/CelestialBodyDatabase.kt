package com.jeffrwatts.celestialnavigation.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jeffrwatts.celestialnavigation.data.CelestialBody
import com.jeffrwatts.celestialnavigation.data.Converters

@Database(entities = [CelestialBody::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CelestialBodyDatabase : RoomDatabase() {

    abstract fun celestialBodyDao(): CelestialBodyDao
}