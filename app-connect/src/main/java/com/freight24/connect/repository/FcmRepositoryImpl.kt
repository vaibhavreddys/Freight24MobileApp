package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.common.manager.SharedPreferencesManager
import com.freight24.rest.api.NotificationServiceApi
import com.freight24.rest.request.RegisterDeviceRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class FcmRepositoryImpl @Inject constructor(
    private val preferencesManager: SharedPreferencesManager,
    private val notificationServiceApi: NotificationServiceApi,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : FcmRepository {

    override suspend fun sendTokenToServerIfNeeded() {
        return withContext(coroutineDispatcher) {
            val token = preferencesManager.firebaseToken
            if (token.isNotEmpty()) {
                notificationServiceApi.registerDevice(
                    RegisterDeviceRequest(token)
                )
            }
        }
    }

}