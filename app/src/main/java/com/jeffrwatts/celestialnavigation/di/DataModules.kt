package com.jeffrwatts.celestialnavigation.di

import android.content.Context
import androidx.room.Room
import com.jeffrwatts.celestialnavigation.BuildConfig
import com.jeffrwatts.celestialnavigation.data.source.*
import com.jeffrwatts.celestialnavigation.data.source.local.SightsDatabase
import com.jeffrwatts.celestialnavigation.data.source.local.SightsLocalDataSource
import com.jeffrwatts.celestialnavigation.data.source.remote.GeoPositionRemoteDataSource
import com.jeffrwatts.celestialnavigation.network.GeoPositionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

//@Qualifier
//@Retention(AnnotationRetention.RUNTIME)
//annotation class RemoteSightsDataSource

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class LocalSightsDataSource

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class RemoteGeoPositionDataSource

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideSightsRepository(
        //@RemoteSightsDataSource remoteDataSource: SightsDataSource,
        @LocalSightsDataSource localDataSource: SightsDataSource,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): SightsRepository {
        return DefaultSightsRepository(localDataSource, ioDispatcher)
    }

    @Singleton
    @Provides
    fun provideGeoPositionRepository(
        @RemoteGeoPositionDataSource remoteDataSource: GeoPositionDataSource,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): GeoPositionRepository {
        return DefaultGeoPositionRepository(remoteDataSource, ioDispatcher)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    //@Singleton
    //@RemoteSightsDataSource
    //@Provides
    //fun provideTasksRemoteDataSource(
    //    database: SightsDatabase,
    //    @IoDispatcher ioDispatcher: CoroutineDispatcher
    //): SightsDataSource {
    //    return SightsLocalDataSource(database.sightsDao(), ioDispatcher)
    //}

    @Singleton
    @LocalSightsDataSource
    @Provides
    fun provideSightsLocalDataSource(
        database: SightsDatabase,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): SightsDataSource {
        return SightsLocalDataSource(database.sightsDao(), ioDispatcher)
    }

    @Singleton
    @RemoteGeoPositionDataSource
    @Provides
    fun provideGeoPositionRemoteDataSource(
        api: GeoPositionApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): GeoPositionDataSource {
        return GeoPositionRemoteDataSource(api, ioDispatcher)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): SightsDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SightsDatabase::class.java,
            "Sights.db"
        ).build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Singleton
    @Provides
    fun provideGeoPositionApi(@ApplicationContext context: Context): GeoPositionApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        return retrofit.create(GeoPositionApi::class.java)
    }
}