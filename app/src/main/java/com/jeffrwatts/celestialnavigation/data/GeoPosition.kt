package com.jeffrwatts.celestialnavigation.data

data class GeoPosition (
    val utc:String,
    val body: String,
    val GHA: Double,
    val dec: Double,
    val distance: Double
)