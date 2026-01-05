package com.freight24.core.network

import android.util.Log
import com.freight24.core.network.api.AuthApi
import com.freight24.core.network.api.LoadBoardApi
import com.freight24.core.network.api.TripApi
import com.freight24.core.network.cache.LoadMemoryCache
import com.freight24.core.network.cache.TripMemoryCache
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    var HOST1 = "192.168.1.2"
    var HOST2 = "192.168.1.8"
    var LOCALHOST = "http://${HOST2}:3000"
    var RENDER = "https://backendapi-qhpo.onrender.com"

    var HOST = RENDER
//    var HOST = LOCALHOST

    var BACKEND_URL = "${HOST}/api/v1/"

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        networkJson: Json
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .client(okHttpClient)
            .addConverterFactory(
                networkJson.asConverterFactory("application/json".toMediaType())
            )
            .build().also {
                Log.d("NetworkModule", "BACKEND_URL: $BACKEND_URL")
            }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideLoadBoardApi(retrofit: Retrofit): LoadBoardApi =
        retrofit.create(LoadBoardApi::class.java)

    @Provides
    @Singleton
    fun provideTripApi(retrofit: Retrofit): TripApi =
        retrofit.create(TripApi::class.java)

    @Provides
    @Singleton
    fun provideLoadMemoryCache(): LoadMemoryCache = LoadMemoryCache()

    @Provides
    @Singleton
    fun provideTripMemoryCache(): TripMemoryCache = TripMemoryCache()

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}