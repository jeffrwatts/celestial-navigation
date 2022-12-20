package com.jeffrwatts.celestialnavigation

import com.jeffrwatts.celestialnavigation.utils.CelNavUtils

data class AddSightTestData(
    val test: String,
    val body: String,
    val utc: String,
    val GHA: Double,
    val dec: Double,
    val distance: Double,
    val lat: Double,
    val lon: Double,
    val Hs: Double,
    val ic: Double,
    val eyeHeight: Int,
    val limb: CelNavUtils.Limb,
    val Hc: Double,
    val Zn: Double,
    val a: Double,
    val direction: CelNavUtils.LOPDirection
)