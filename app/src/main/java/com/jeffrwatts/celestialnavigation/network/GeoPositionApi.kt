package com.jeffrwatts.celestialnavigation.network

import com.jeffrwatts.celestialnavigation.data.GeoPosition
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoPositionApi {
    @GET("/")
    suspend fun getGeographicalPosition(@Query("body") body:String, @Query("utc") utc:Double): GeoPosition
}