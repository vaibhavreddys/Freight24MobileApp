package com.freight24.network.provider

import retrofit2.HttpException

class StatusCodeRetryCallImpl(
    private val statusCodes: List<Int>,
    private val retryBlock: suspend () -> Boolean,
) : RetryCall {
    override suspend fun invoke(throwable: Throwable): Boolean {
        if (throwable is HttpException && statusCodes.contains(throwable.code())) {
            return retryBlock()
        }
        return false
    }
}
