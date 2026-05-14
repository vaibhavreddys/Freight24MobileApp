package com.freight24.trucker.feature.login.data.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.auth.SessionManager
import com.freight24.rest.api.TruckerLoginApi
import com.freight24.rest.request.OtpRequest
import com.freight24.rest.request.VerifyOtpRequest
import com.freight24.rest.response.LoginResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LoginRepositoryImpl
    @Inject
    constructor(
        private val loginApi: TruckerLoginApi,
        private val sessionManager: SessionManager,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : LoginRepository {
        override suspend fun requestOtp(phoneNumber: String): LoginResponse {
            return withContext(ioDispatcher) {
                loginApi.requestOtp(OtpRequest(phoneNumber))
            }
        }

        override suspend fun verifyOtp(
            phoneNumber: String,
            otp: String,
        ): LoginResponse {
            return withContext(ioDispatcher) {
                val installationId = sessionManager.ensureInstallationId()
                loginApi.verifyOtp(
                    VerifyOtpRequest(
                        phone_number = phoneNumber,
                        otp = otp,
                        role_id = 2,
                        installation_id = installationId,
                    ),
                ).also { response ->
                    val data = response.data
                    val accessToken = data?.access_token ?: data?.token
                    val userId = data?.user?.id

                    if (response.success && data != null && !accessToken.isNullOrBlank() && !userId.isNullOrBlank()) {
                        sessionManager.saveSession(
                            phone = phoneNumber,
                            userId = userId,
                            authData = data,
                            installationId = installationId,
                        )
                    }
                }
            }
        }
    }
