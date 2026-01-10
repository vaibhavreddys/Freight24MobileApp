package com.freight24.network.provider

interface RetryCall {
    suspend operator fun invoke(throwable: Throwable): Boolean
}
