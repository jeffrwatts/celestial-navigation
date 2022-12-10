package com.jeffrwatts.celestialnavigation.data.source

import android.content.Context
import com.google.android.gms.maps.model.LatLng

class DefaultSightPrefsRepository (context: Context) : SightPrefsRepository {

    companion object {
        const private val ASSUMED_LATITUDE = "AssumedLatitude"
        const private val ASSUMED_LONGITUDE = "AssumedLongitude"
        const private val INDEX_CORRECTION = "IndexCorrection"
        const private val EYE_HEIGHT = "EyeHeight"
    }

    val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    override fun getAssumedPosition(): LatLng {
        val lat = sharedPreferences.getFloat(ASSUMED_LATITUDE, 0.0f)
        val lon = sharedPreferences.getFloat(ASSUMED_LONGITUDE, 0.0f)
        return LatLng(lat.toDouble(), lon.toDouble())
    }

    override fun setAssumedPosition(assumedPosition: LatLng) {
        val editor = sharedPreferences.edit()
        editor.putFloat(ASSUMED_LATITUDE, assumedPosition.latitude.toFloat())
        editor.putFloat(ASSUMED_LONGITUDE, assumedPosition.longitude.toFloat())
        editor.apply()
    }

    override fun getIC(): Double {
        return sharedPreferences.getFloat(INDEX_CORRECTION, 0.0f).toDouble()
    }

    override fun setIC(ic: Double) {
        val editor = sharedPreferences.edit()
        editor.putFloat(INDEX_CORRECTION, ic.toFloat())
        editor.apply()
    }

    override fun getEyeHeight(): Int {
        return sharedPreferences.getInt(EYE_HEIGHT, 0)
    }

    override fun setEyeHeight(eyeHeight: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(EYE_HEIGHT, eyeHeight)
        editor.apply()
    }
}