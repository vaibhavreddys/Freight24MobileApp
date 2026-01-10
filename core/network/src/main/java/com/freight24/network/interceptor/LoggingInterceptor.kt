package com.freight24.network.interceptor

import android.util.Log
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject

internal fun getHttpLoggingInterceptor(loggerTag: String): HttpLoggingInterceptor {
    return HttpLoggingInterceptor { message ->
        Log.d(loggerTag, formatLogMessage(message))
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
}

private fun formatLogMessage(message: String): String {
    return when {
        // Format JSON response bodies
        message.startsWith("{") || message.startsWith("[") -> {
            try {
                if (message.startsWith("{")) {
                    JSONObject(message).toString(2)
                } else {
                    JSONArray(message).toString(2)
                }
            } catch (_: Exception) {
                message
            }
        }

        // Format cookie headers
        message.startsWith("Cookie:") -> {
            val cookieValue = message.substringAfter("Cookie: ")
            val cookies = cookieValue.split("; ").joinToString("\n") { "  - $it" }
            "Cookie:\n$cookies"
        }

        else -> message
    }
}
