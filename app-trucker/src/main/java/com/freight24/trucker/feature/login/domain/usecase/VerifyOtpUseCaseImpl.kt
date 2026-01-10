package com.freight24.trucker.feature.login.domain.usecase

import com.freight24.auth.AuthDataStore
import com.freight24.trucker.feature.login.data.models.VerifyOtpResult
import com.freight24.trucker.feature.login.data.repository.LoginRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class VerifyOtpUseCaseImpl
    @Inject
    constructor(
        private val loginRepository: LoginRepository,
        private val authDataStore: AuthDataStore,
    ) : VerifyOtpUseCase {
        override suspend fun invoke(params: VerifyOtpUseCaseParams): Result<VerifyOtpResult> =
            runCatching {
                val response = loginRepository.verifyOtp(params.phoneNumber, params.otp)

                if (!response.success) {
                    return@runCatching VerifyOtpResult.OtpExpired(response.message)
                }

                val authData =
                    response.data
                        ?: return@runCatching VerifyOtpResult.OtpExpired("Missing data in response")

                val token =
                    authData.token
                        ?: return@runCatching VerifyOtpResult.InvalidOtp("Token not provided by server")

                val userId = authData.user?.id.orEmpty()
                authDataStore.saveAuthData(token, params.phoneNumber, userId)
                VerifyOtpResult.Success(true, token)
            }.recover { e ->
                VerifyOtpResult.Error(e.message.orEmpty())
            }
    }
