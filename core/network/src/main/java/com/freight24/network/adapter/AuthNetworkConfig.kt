package com.freight24.network.adapter

import okhttp3.Interceptor

data class AuthNetworkConfig(
    val interceptor: Interceptor,
)
