package com.freight24.connect.feature.login.domain.usecase

import com.freight24.auth.AuthDataStore
import com.freight24.connect.feature.login.domain.model.VerifyOtpResult
import com.freight24.connect.feature.login.repository.LoginConnectRepository
import javax.inject.Inject

internal class VerifyOtpUseCaseImpl
    @Inject
    constructor(
        private val loginConnectRepository: LoginConnectRepository,
        private val authDataStore: AuthDataStore,
    ) : VerifyOtpUseCase {
        override suspend fun invoke(params: VerifyOtpUseCaseParams): Result<VerifyOtpResult> {
            return loginConnectRepository.verifyOtp(params.phoneNumber, params.otp)
                .map { authInfo ->
                    authDataStore.saveAuthData(authInfo.token, params.phoneNumber, authInfo.userId)
                    VerifyOtpResult.Success(true, authInfo.token)
                }
                .recover { e ->
                    VerifyOtpResult.Error(e.message ?: "Unknown error")
                }
        }
    }
