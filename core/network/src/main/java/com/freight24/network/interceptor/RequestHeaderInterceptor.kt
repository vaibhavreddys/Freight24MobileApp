package com.freight24.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException
import kotlin.jvm.Throws

class RequestHeaderInterceptor(
    val headers: Map<String, String>,
) : Interceptor, BaseInterceptor() {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain
            .request()
            .newBuilder()
            .apply {
                headers.forEach { (name, value) ->
                    safeHeader(name, value)
                }
            }
            .build()
            .let { chain.proceed(it) }
    }
}
