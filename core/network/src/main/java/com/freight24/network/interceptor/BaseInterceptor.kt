package com.freight24.network.interceptor

import android.util.Log
import okhttp3.Request

open class BaseInterceptor {
    fun Request.Builder.safeHeader(
        name: String?,
        value: String?,
    ) = apply {
        if (!name.isNullOrBlank() && !value.isNullOrBlank()) {
            try {
                Log.d("BaseInterceptor", "Adding Header: $name: $value")
                addHeader(name, value)
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.d("BaseInterceptor", "Error Adding Header: $name: $value")
            }
        }
    }
}
