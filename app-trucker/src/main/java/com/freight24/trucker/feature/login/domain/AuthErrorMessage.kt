package com.freight24.trucker.feature.login.domain

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Turns a failed auth call into something worth showing a driver.
 *
 * Two rules, both learned the hard way:
 *
 *  1. **Never surface an exception message.** OkHttp writes things like
 *     "Failed to connect to localhost/127.0.0.1:3000" and
 *     "Unable to resolve host", and those strings used to land on the OTP
 *     screen verbatim — telling the user our internal address, which is both
 *     noise to them and a small amount of reconnaissance for anyone else.
 *  2. **Say what to do next.** "Check your signal and try again" tells a
 *     driver standing in a yard with one bar what to do; a raw socket
 *     message does not.
 *
 * Server-authored copy ("Invalid OTP", "Too many requests") is deliberately
 * NOT genericised — that is product messaging the backend chose on purpose,
 * and only transport failures come through here.
 */
internal object AuthErrorMessage {
    const val OFFLINE =
        "No internet connection. Check your network and try again."
    const val TIMEOUT =
        "This is taking longer than expected. Please try again."
    const val UNREACHABLE =
        "We couldn't reach Freight24 right now. Please try again."
    const val RATE_LIMITED =
        "Too many attempts. Please wait a moment and try again."

    /**
     * @return a message safe and useful to put in front of a truck driver.
     */
    fun from(throwable: Throwable): String =
        when (throwable) {
            is UnknownHostException -> OFFLINE
            is SocketTimeoutException -> TIMEOUT
            is HttpException ->
                when (throwable.code()) {
                    429 -> RATE_LIMITED
                    in 500..599 -> UNREACHABLE
                    // A 4xx we do not recognise still came back from the server, so
                    // its own body was already offered upstream and never reaches
                    // this fallback. Anything landing here is transport-level.
                    else -> UNREACHABLE
                }
            is ConnectException -> UNREACHABLE
            is IOException -> UNREACHABLE
            else -> UNREACHABLE
        }

    /**
     * Guards the "server replied, but the body had nothing usable in it"
     * case, which would otherwise render an empty error block.
     */
    fun orGeneric(message: String?): String = message?.takeIf { it.isNotBlank() } ?: UNREACHABLE
}
