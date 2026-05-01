package com.freight24.connect.feature.login.domain.usecase

import com.freight24.connect.feature.login.domain.model.VerifyOtpResult
import com.freight24.connect.feature.login.repository.LoginConnectRepository
import javax.inject.Inject

internal class VerifyOtpUseCaseImpl
    @Inject
    constructor(
        private val loginConnectRepository: LoginConnectRepository,
    ) : VerifyOtpUseCase {
        override suspend fun invoke(params: VerifyOtpUseCaseParams): Result<VerifyOtpResult> {
            return loginConnectRepository.verifyOtp(params.phoneNumber, params.otp)
                .map { authInfo ->
                    VerifyOtpResult.Success(true, authInfo.token)
                }
                .recover { e ->
                    VerifyOtpResult.Error(e.message ?: "Unknown error")
                }
        }
    }
