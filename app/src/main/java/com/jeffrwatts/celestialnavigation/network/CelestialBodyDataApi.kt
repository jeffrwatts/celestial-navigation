package com.jeffrwatts.celestialnavigation.network

import com.jeffrwatts.celestialnavigation.data.CelestialBody
import retrofit2.http.GET
import retrofit2.http.Query

interface CelestialBodyDataApi {
    @GET("/")
    suspend fun getCelestialObjData(@Query("lat") lat:Double,
                                    @Query("lon") lon:Double,
                                    @Query("riseStart") riseStart:Double,
                                    @Query("riseDays") riseDays:Int): List<CelestialBody>
}