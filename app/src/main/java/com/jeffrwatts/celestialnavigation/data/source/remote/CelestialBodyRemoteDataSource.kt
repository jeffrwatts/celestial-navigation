package com.jeffrwatts.celestialnavigation.data.source.remote

import android.content.Context
import android.os.Build
import android.util.Log
import com.jeffrwatts.celestialnavigation.data.CelestialBody
import com.jeffrwatts.celestialnavigation.data.source.CelestialBodyDataSource
import com.jeffrwatts.celestialnavigation.data.Result
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets


class CelestialBodyRemoteDataSource internal constructor(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CelestialBodyDataSource {

    override fun getCelestialBodiesStream(): Flow<Result<List<CelestialBody>>> {
        // Not sure if Flow makes sense from remote.. it is not called.
        return MutableStateFlow(Result.Success(emptyList()))
    }

    override suspend fun getCelestialBodies(): Result<List<CelestialBody>> {
        delay(5000) // Simulate network call.
        val celestialBodies = loadFromAssets()
        return Result.Success(celestialBodies)
    }

    override suspend fun saveCelestialBody(body: CelestialBody) {
        // NO-OP
    }

    override suspend fun deleteAllCelestialBodies() {
        // NO-OP
    }

    private fun loadFromAssets():List<CelestialBody> {
        try {
            val inputsStream = context.assets.open("celestialbodies.json")
            val celestialBodiesString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                String(inputsStream.readAllBytes(), StandardCharsets.UTF_8)
            } else {
                ""
            }
            val moshi = Moshi.Builder().build()
            val listOfBodiesType: Type = Types.newParameterizedType(MutableList::class.java, CelestialBody::class.java)
            val jsonAdapter: JsonAdapter<List<CelestialBody>> = moshi.adapter(listOfBodiesType)
            val celestialBodies = jsonAdapter.fromJson(celestialBodiesString)
            return celestialBodies ?: emptyList()
        } catch (e: Exception) {
            Log.e("TAG", "Error", e)
            return emptyList()
        }
    }
}


