package com.freight24.network.provider

import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Response

interface SafeCallProvider {
    suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher,
        apiCall: suspend () -> Response<T>,
    ): Result<T>

    suspend fun <T> safeApiCallWithRetry(
        dispatcher: CoroutineDispatcher,
        apiCall: suspend () -> Response<T>,
        retryCall: RetryCall,
        maxRetries: Int = 1,
    ): Result<T>
}
