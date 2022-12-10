package com.jeffrwatts.celestialnavigation.data.source

import com.google.android.gms.maps.model.LatLng

interface SightPrefsRepository {
    fun getAssumedPosition(): LatLng
    fun setAssumedPosition(assumedPosition: LatLng)
    fun getIC(): Double
    fun setIC(ic: Double)
    fun getEyeHeight(): Int
    fun setEyeHeight(eyeHeight: Int)
}