package com.freight24.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit

interface RetrofitFactory {
    fun create(
        baseUrl: String,
        loggerTag: String?,
        clientConfigurator: OkHttpClient.Builder.() -> Unit = {},
        retrofitConfigurator: Retrofit.Builder.() -> Unit = {},
    ): Retrofit
}
