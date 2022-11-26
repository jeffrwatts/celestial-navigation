package com.jeffrwatts.celestialnavigation

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CelNavApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}