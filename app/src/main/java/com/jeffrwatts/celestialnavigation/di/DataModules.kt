package com.jeffrwatts.celestialnavigation.di

import android.content.Context
import androidx.room.Room
import com.jeffrwatts.celestialnavigation.data.source.DefaultSightsRepository
import com.jeffrwatts.celestialnavigation.data.source.SightsDataSource
import com.jeffrwatts.celestialnavigation.data.source.SightsRepository
import com.jeffrwatts.celestialnavigation.data.source.local.SightsDatabase
import com.jeffrwatts.celestialnavigation.data.source.local.SightsLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Qualifier
import javax.inject.Singleton

//@Qualifier
//@Retention(AnnotationRetention.RUNTIME)
//annotation class RemoteSightsDataSource

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class LocalSightsDataSource

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideTasksRepository(
        //@RemoteSightsDataSource remoteDataSource: SightsDataSource,
        @LocalSightsDataSource localDataSource: SightsDataSource,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): SightsRepository {
        return DefaultSightsRepository(localDataSource, ioDispatcher)
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
    fun provideTasksLocalDataSource(
        database: SightsDatabase,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): SightsDataSource {
        return SightsLocalDataSource(database.sightsDao(), ioDispatcher)
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