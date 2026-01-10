package com.freight24.network.error

import java.io.IOException

sealed class NetworkException(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause) {
    data class HttpException(
        val code: Int,
        val errorBody: String?,
    ) : NetworkException("HTTP $code error $errorBody")

    data object Timeout : NetworkException("Request timeout") {
        private fun readResolve(): Any = Timeout
    }

    data object UnknownHost : NetworkException("Unknown host") {
        private fun readResolve(): Any = UnknownHost
    }

    data class Unknown(val throwable: Throwable?) : NetworkException(throwable?.localizedMessage, throwable)
}
