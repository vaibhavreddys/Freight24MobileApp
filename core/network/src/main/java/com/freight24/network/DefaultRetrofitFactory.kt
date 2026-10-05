package com.freight24.network

import com.freight24.network.adapter.NetworkConfig
import com.freight24.network.interceptor.getHttpLoggingInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class DefaultRetrofitFactory(
    private val okHttpClient: OkHttpClient,
    private val networkConfig: NetworkConfig,
    private val json: Json,
) : RetrofitFactory {
    override fun create(
        baseUrl: String,
        loggerTag: String?,
        clientConfigurator: OkHttpClient.Builder.() -> Unit,
        retrofitConfigurator: Retrofit.Builder.() -> Unit,
    ): Retrofit {
        val client =
            okHttpClient
                .newBuilder()
                .apply { clientConfigurator }
                .apply {
                    if (networkConfig.logsEnabled) {
                        addNetworkInterceptor(
                            getHttpLoggingInterceptor(loggerTag ?: networkConfig.logsTag),
                        )
                    }
                }
                .build()

        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .apply(retrofitConfigurator)
            .build()
    }
}
