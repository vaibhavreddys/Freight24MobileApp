package com.freight24.core.network

import com.freight24.core.network.api.AuthApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun providesOkHttpCallFactory(): Call.Factory = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            },
        )
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okhttpCallFactory: Call.Factory,
        networkJson: Json,
    ): Retrofit = Retrofit.Builder()
        // You should configure the Base URL via BuildConfig in a real app,
        // passing it as a parameter or injecting it.
        // For now, using a placeholder.
//        .baseUrl("https://backendapi-qhpo.onrender.com/api/v1/")
        .baseUrl("http://192.168.1.38:3000/api/v1/")
        .callFactory(okhttpCallFactory)
        .addConverterFactory(
            networkJson.asConverterFactory("application/json".toMediaType()),
        )
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

//    @Provides
//    @Singleton
//    fun provideTripApi(retrofit: Retrofit): TripApi {
//        return retrofit.create(TripApi::class.java)
//    }
}