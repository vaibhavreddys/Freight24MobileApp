package com.freight24.connect.repository

internal interface FcmRepository {
    suspend fun sendTokenToServerIfNeeded()
}
