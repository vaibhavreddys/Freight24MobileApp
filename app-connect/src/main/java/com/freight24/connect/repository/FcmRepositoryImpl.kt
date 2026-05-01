package com.freight24.connect.repository

import android.util.Log
import com.freight24.common.di.IoDispatcher
import com.freight24.auth.SessionManager
import com.freight24.common.manager.SharedPreferencesManager
import com.freight24.rest.api.NotificationServiceApi
import com.freight24.rest.request.RegisterDeviceRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

internal class FcmRepositoryImpl
    @Inject
    constructor(
        private val preferencesManager: SharedPreferencesManager,
        private val sessionManager: SessionManager,
        private val notificationServiceApi: NotificationServiceApi,
        @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
    ) : FcmRepository {
        override suspend fun sendTokenToServerIfNeeded() {
            return withContext(coroutineDispatcher) {
                val token = preferencesManager.firebaseToken
                if (token.isNotEmpty()) {
                    val installationId = sessionManager.ensureInstallationId()
                    val request =
                        RegisterDeviceRequest(
                            deviceToken = token,
                            deviceId = installationId,
                        )

                    runCatching {
                        notificationServiceApi.registerDevice(request)
                    }.onFailure { throwable ->
                        if (throwable is HttpException && throwable.code() == 401) {
                            Log.w("FcmRepository", "Device registration returned 401, attempting refresh")
                            if (sessionManager.refreshSession()) {
                                runCatching {
                                    notificationServiceApi.registerDevice(request)
                                }.onFailure { retryThrowable ->
                                    Log.w("FcmRepository", "Device registration retry failed", retryThrowable)
                                }
                            }
                        } else {
                            Log.w("FcmRepository", "Device registration failed", throwable)
                        }
                    }
                }
            }
        }
    }
