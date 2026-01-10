package com.freight24.network.provider

import com.freight24.network.error.NetworkException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.single
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class SafeCallProviderImpl
    @Inject
    constructor() : SafeCallProvider {
        override suspend fun <T> safeApiCall(
            dispatcher: CoroutineDispatcher,
            apiCall: suspend () -> Response<T>,
        ): Result<T> =
            makeApiCall(dispatcher, apiCall)
                .retry(1)
                .mapException()
                .single()

        override suspend fun <T> safeApiCallWithRetry(
            dispatcher: CoroutineDispatcher,
            apiCall: suspend () -> Response<T>,
            retryCall: RetryCall,
            maxRetries: Int,
        ): Result<T> =
            makeApiCall(dispatcher, apiCall)
                .retry(maxRetries.toLong()) { e -> retryCall(e) }
                .mapException()
                .single()

        private fun <T> makeApiCall(
            dispatcher: CoroutineDispatcher,
            apiCall: suspend () -> Response<T>,
        ) = flow<Result<T>> {
            val response = apiCall()
            val result = response.body()
            if (result != null) {
                emit(Result.success(result))
            } else {
                throw HttpException(response)
            }
        }.flowOn(dispatcher)

        private fun <T> Flow<Result<T>>.mapException(): Flow<Result<T>> =
            catch { ex ->
                emit(Result.failure(ex.toParsedException()))
            }

        private fun Throwable.toParsedException(): NetworkException {
            return when (this) {
                is HttpException -> {
                    val errorBody = this.response()?.errorBody()?.string()
                    NetworkException.HttpException(this.code(), errorBody)
                }
                is UnknownHostException -> {
                    NetworkException.UnknownHost
                }
                is SocketTimeoutException -> {
                    NetworkException.Timeout
                }
                else -> {
                    NetworkException.Unknown(this)
                }
            }
        }
    }
