package com.freight24.core.network

import com.freight24.core.network.api.AuthApi
import com.freight24.core.network.api.LoadBoardApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
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
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        networkJson: Json
    ): Retrofit =
        Retrofit.Builder()
//            .baseUrl("http://192.168.1.5:3000/api/v1/")
            .baseUrl("http://192.168.1.14:3000/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(
                networkJson.asConverterFactory("application/json".toMediaType())
            )
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideLoadBoardApi(retrofit: Retrofit): LoadBoardApi =
        retrofit.create(LoadBoardApi::class.java)
}

/*

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

//    @Provides
//    @Singleton
//    fun provideOkHttpClient(
//        authInterceptor: com.freight24.core.auth.AuthInterceptor
//    ): OkHttpClient {
//        Log.e("NetworkModule", "Using AuthInterceptor directly")
//
//        return OkHttpClient.Builder()
//            .addInterceptor(authInterceptor) // ✅ GUARANTEED
//            .build()
//    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        networkJson: Json,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://192.168.1.5:3000/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(
                networkJson.asConverterFactory("application/json".toMediaType())
            )
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideLoadBoardApi(retrofit: Retrofit): LoadBoardApi =
        retrofit.create(LoadBoardApi::class.java)
}

 */

/*

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
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        networkJson: Json,
    ): Retrofit = Retrofit.Builder()
        // You should configure the Base URL via BuildConfig in a real app,
        // passing it as a parameter or injecting it.
        // For now, using a placeholder.
//        .baseUrl("https://backendapi-qhpo.onrender.com/api/v1/")
        .baseUrl("http://192.168.1.5:3000/api/v1/")
        .client(okHttpClient)
        .addConverterFactory(
            networkJson.asConverterFactory("application/json".toMediaType()),
        )
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        interceptors: Set<@JvmSuppressWildcards Interceptor>
    ): OkHttpClient {
        Log.e("NetworkModule", "Interceptors count = ${interceptors.size}")
        interceptors.forEach {
            Log.e("NetworkModule", "Interceptor = ${it::class.java.name}")
        }

        return OkHttpClient.Builder()
            .apply { interceptors.forEach { addInterceptor(it) } }
            .build()
    }


    @Provides
    @Singleton
    fun provideLoadBoardApi(retrofit: Retrofit): LoadBoardApi =
        retrofit.create(LoadBoardApi::class.java)


//    @Provides
//    @Singleton
//    fun provideEmptyInterceptorSet(): Set<Interceptor> = emptySet()

//    @Provides
//    @Singleton
//    fun provideTripApi(retrofit: Retrofit): TripApi {
//        return retrofit.create(TripApi::class.java)
//    }

}


 */