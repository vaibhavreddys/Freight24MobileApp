package com.freight24.connect.feature.login.repository

import android.util.Log
import com.freight24.common.di.IoDispatcher
import com.freight24.connect.feature.login.domain.model.AuthInfo
import com.freight24.rest.api.LoginConnectApi
import com.freight24.rest.request.OtpRequest
import com.freight24.rest.request.VerifyOtpRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LoginConnectRepositoryImpl
    @Inject
    constructor(
        private val loginConnectApi: LoginConnectApi,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : LoginConnectRepository {
        override suspend fun requestOtp(phoneNumber: String): Result<Unit> {
            return withContext(ioDispatcher) {
                try {
                    val response = loginConnectApi.requestOtp(OtpRequest(phoneNumber))
                    if (response.success) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(response.message))
                    }
                } catch (e: Exception) {
                    Log.e("LoginConnectRepository", e.message.orEmpty())
                    Result.failure(e)
                }
            }
        }

        override suspend fun verifyOtp(
            phoneNumber: String,
            otp: String,
        ): Result<AuthInfo> {
            return withContext(ioDispatcher) {
                try {
                    val response =
                        loginConnectApi.verifyOtp(
                            VerifyOtpRequest(
                                phone_number = phoneNumber,
                                otp = otp,
                            ),
                        )
                    if (response.success) {
                        val data = response.data
                        if (data != null && !data.token.isNullOrEmpty() && !data.user?.id.isNullOrEmpty()) {
                            Result.success(
                                AuthInfo(
                                    token = data.token.orEmpty(),
                                    userId = data.user?.id.orEmpty(),
                                ),
                            )
                        } else {
                            Result.failure(Exception("Invalid server response"))
                        }
                    } else {
                        Result.failure(Exception(response.message))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }
    }
