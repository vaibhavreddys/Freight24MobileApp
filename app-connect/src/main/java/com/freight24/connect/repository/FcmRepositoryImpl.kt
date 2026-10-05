package com.freight24.connect.repository

import android.content.Context
import android.os.Build
import android.util.Log
import com.freight24.auth.SessionManager
import com.freight24.common.di.IoDispatcher
import com.freight24.common.manager.SharedPreferencesManager
import com.freight24.connect.BuildConfig
import com.freight24.rest.api.NotificationServiceApi
import com.freight24.rest.request.RegisterDeviceRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

internal class FcmRepositoryImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
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
                    val appVersion =
                        try {
                            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
                        } catch (_: Exception) {
                            "1.0"
                        }
                    val appVariant =
                        when (BuildConfig.FLAVOR) {
                            "qa" -> "QA"
                            "prod" -> "PRODUCTION"
                            else -> BuildConfig.FLAVOR.uppercase()
                        }
                    val request =
                        RegisterDeviceRequest(
                            deviceToken = token,
                            platform = "android",
                            appVariant = appVariant,
                            deviceId = installationId,
                            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                            osVersion = Build.VERSION.RELEASE,
                            appVersion = appVersion,
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
