package com.freight24.network

import com.freight24.network.adapter.NetworkConfig
import com.freight24.network.interceptor.getHttpLoggingInterceptor
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DefaultRetrofitFactory(
    val okHttpClient: OkHttpClient,
    val networkConfig: NetworkConfig,
) : RetrofitFactory {
    override fun create(
        baseUrl: String,
        loggerTag: String?,
        clientConfigurator: OkHttpClient.Builder.() -> Unit,
        retrofitConfigurator: Retrofit.Builder.() -> Unit,
    ): Retrofit {
        val gson =
            GsonBuilder()
                .setLenient()
                .create()

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

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .apply(retrofitConfigurator)
            .build()
    }
}
