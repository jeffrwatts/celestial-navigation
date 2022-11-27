package com.jeffrwatts.celestialnavigation.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jeffrwatts.celestialnavigation.utils.CelNavUtils
import java.util.*

/**
 * Immutable model class for a Sight. In order to compile with Room, we can't use @JvmOverloads to
 * generate multiple constructors.
 *
 * @param title title of the task
 * @param Hs Sextant Height measurement of the task
 * @param assumedLat Assumed Latitude.
 * @param assumedLon Assumed Longitude.
 * @param intercept distance in nautical miles from assumed position.
 * @param Zn angle towards or away from assumed position.
 * @param isCompleted whether or not this task is completed
 * @param id id of the task
 */
@Entity(tableName = "sights")
data class Sight @JvmOverloads constructor(
    // Sextant Inputs
    @ColumnInfo(name = "Hs") var Hs: Double = 0.0,
    @ColumnInfo(name = "ic") var ic: Double = 0.0,
    @ColumnInfo(name = "eyeHeight") var eyeHeight: Int = 0,
    @ColumnInfo(name = "limb") var limb: CelNavUtils.Limb = CelNavUtils.Limb.Center,

    // Celestial Body
    @ColumnInfo(name = "celestialBody") var celestialBody: String = "",
    @ColumnInfo(name = "utc") var utc: String = "",
    @ColumnInfo(name = "distance") var distance: Double = 0.0,
    @ColumnInfo(name = "equitorialRadius") var equitorialRadius: Double = 0.0,
    @ColumnInfo(name = "gha") var gha: Double = 0.0,
    @ColumnInfo(name = "dec") var dec: Double = 0.0,

    // Assumed or DR position
    @ColumnInfo(name = "lat") var lat: Double = 0.0,
    @ColumnInfo(name = "lon") var lon: Double = 0.0,

    @ColumnInfo(name = "activated") var isActive: Boolean = false,
    @PrimaryKey @ColumnInfo(name = "entryid") var id: String = UUID.randomUUID().toString())

