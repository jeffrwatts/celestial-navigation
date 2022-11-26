package com.jeffrwatts.celestialnavigation.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jeffrwatts.celestialnavigation.data.Sight

/**
 * The Room Database that contains the Task table.
 *
 * Note that exportSchema should be true in production databases.
 */
@Database(entities = [Sight::class], version = 1, exportSchema = false)
abstract class SightsDatabase : RoomDatabase() {

    abstract fun sightsDao(): SightsDao
}