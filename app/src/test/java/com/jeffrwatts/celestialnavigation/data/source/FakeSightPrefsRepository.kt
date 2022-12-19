package com.jeffrwatts.celestialnavigation.data.source

import com.google.android.gms.maps.model.LatLng

class FakeSightPrefsRepository : SightPrefsRepository {

    private var _assumedPosition: LatLng = LatLng(0.0,0.0)
    private var _ic: Double = 0.0
    private var _eyeHeight: Int = 0

    override fun getAssumedPosition(): LatLng {
        return _assumedPosition
    }

    override fun setAssumedPosition(assumedPosition: LatLng) {
        _assumedPosition = assumedPosition
    }

    override fun getIC(): Double {
        return _ic
    }

    override fun setIC(ic: Double) {
        _ic = ic
    }

    override fun getEyeHeight(): Int {
        return _eyeHeight
    }

    override fun setEyeHeight(eyeHeight: Int) {
        _eyeHeight = eyeHeight
    }
}