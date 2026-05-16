package com.freight24.auth

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor
    @Inject
    constructor(
        private val sessionManager: SessionManager,
        private val tokenManager: TokenManager,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            val token =
                runBlocking {
                    tokenManager.authToken.first()
                }

            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val response = chain.proceed(requestBuilder.build())

            if (response.code == 401) {
                val refreshed =
                    runBlocking {
                        sessionManager.refreshSession()
                    }

                if (refreshed) {
                    val newToken =
                        runBlocking {
                            tokenManager.authToken.first()
                        }

                    if (!newToken.isNullOrBlank()) {
                        val retryRequest =
                            originalRequest.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .build()
                        return chain.proceed(retryRequest)
                    }
                }

                runBlocking {
                    tokenManager.clearAuth()
                }
            }

            return response
        }
    }
