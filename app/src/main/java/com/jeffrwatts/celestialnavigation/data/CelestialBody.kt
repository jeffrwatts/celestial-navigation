package com.jeffrwatts.celestialnavigation.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CelestialObjectBodyType {
    Sun,
    Moon,
    Planet,
    Star
}

@Entity(tableName = "celestialBodies")
data class CelestialBody @JvmOverloads constructor(
    @PrimaryKey @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "order") var order: Int,
    @ColumnInfo(name = "objtype") var objtype: CelestialObjectBodyType,
    @ColumnInfo(name = "magnitude") var magnitude: Double,
    @ColumnInfo(name = "ra") var ra: Double,
    @ColumnInfo(name = "dec") var dec: Double,
    @ColumnInfo(name = "distance") var distance: Double)

