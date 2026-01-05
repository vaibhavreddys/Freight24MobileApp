package com.freight24.core.auth

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager // Use TokenManager directly to break the cycle
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 1. Get the current token synchronously
        val token = runBlocking {
            tokenManager.authToken.first()
        }

        // 2. Add Authorization header if token exists
        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        // 3. Handle 401 Unauthorized
        if (response.code == 401) {
            runBlocking {
                // Clear token
                tokenManager.clearAuth()
            }
        }

        return response
    }
}
