package com.freight24.network.di

import com.freight24.network.DefaultRetrofitFactory
import com.freight24.network.RetrofitFactory
import com.freight24.network.adapter.AuthNetworkConfig
import com.freight24.network.adapter.NetworkConfig
import com.freight24.network.interceptor.RequestHeaderInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providesJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @DefaultNetwork
    fun provideDefaultOkHttpClient(config: NetworkConfig): OkHttpClient {
        val requestHeaderInterceptor =
            RequestHeaderInterceptor(
                mapOf(
                    "Content-Type" to "application/json",
                ),
            )
        return OkHttpClient.Builder()
            .addInterceptor(requestHeaderInterceptor)
            .connectTimeout(config.timeout, TimeUnit.SECONDS)
            .readTimeout(config.timeout, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @DefaultNetwork
    fun providesDefaultRetrofitFactory(
        @DefaultNetwork okHttpClient: OkHttpClient,
        config: NetworkConfig,
        json: Json
    ): RetrofitFactory =
        DefaultRetrofitFactory(
            okHttpClient,
            config,
            json
        )

    @Provides
    fun providesAuthenticatedOkHttpClient(
        config: NetworkConfig,
        authNetworkConfig: AuthNetworkConfig,
    ): OkHttpClient {
        val requestHeaderInterceptor = RequestHeaderInterceptor(config.headers)
        return OkHttpClient.Builder()
            .addInterceptor(requestHeaderInterceptor)
            .addInterceptor(authNetworkConfig.interceptor)
            .connectTimeout(config.timeout, TimeUnit.SECONDS)
            .readTimeout(config.timeout, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    fun providesAuthenticatedRetrofitFactory(
        okHttpClient: OkHttpClient,
        config: NetworkConfig,
        json: Json
    ): RetrofitFactory =
        DefaultRetrofitFactory(
            okHttpClient,
            config,
            json
        )
}
