package com.jeffrwatts.celestialnavigation.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

enum class CelestialObjectBodyType {
    Sun,
    Moon,
    Planet,
    Star
}

enum class RiseSetType {
    Rise,
    Set
}

data class RiseSet (
    val utc: Double,
    val riseset: RiseSetType
)

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<RiseSet>? {
        return if (value != null) {
            val moshi = Moshi.Builder().build()
            val listOfRiseSetType: Type = Types.newParameterizedType(MutableList::class.java, RiseSet::class.java)
            val jsonAdapter: JsonAdapter<List<RiseSet>> = moshi.adapter(listOfRiseSetType)
            return jsonAdapter.fromJson(value)
        } else {
            null
        }
    }

    @TypeConverter
    fun toString(value: List<RiseSet>?): String? {
        return if (value != null) {
            val moshi = Moshi.Builder().build()
            val listOfRiseSetType: Type =
                Types.newParameterizedType(MutableList::class.java, RiseSet::class.java)
            val jsonAdapter: JsonAdapter<List<RiseSet>> = moshi.adapter(listOfRiseSetType)
            return jsonAdapter.toJson(value)
        } else {
            null
        }
    }
}

@Entity(tableName = "celestialBodies")
data class CelestialBody @JvmOverloads constructor(
    @PrimaryKey @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "order") var order: Int,
    @ColumnInfo(name = "objtype") var objtype: CelestialObjectBodyType,
    @ColumnInfo(name = "magnitude") var magnitude: Double,
    @ColumnInfo(name = "ra") var ra: Double,
    @ColumnInfo(name = "dec") var dec: Double,
    @ColumnInfo(name = "distance") var distance: Double,
    @ColumnInfo(name = "riseset") var riseset: List<RiseSet>)

