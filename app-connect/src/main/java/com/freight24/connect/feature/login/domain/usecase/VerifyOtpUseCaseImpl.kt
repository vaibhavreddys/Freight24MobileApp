package com.freight24.connect.feature.login.domain.usecase

import com.freight24.auth.AuthDataStore
import com.freight24.connect.feature.login.domain.model.VerifyOtpResult
import com.freight24.connect.feature.login.repository.LoginConnectRepository
import javax.inject.Inject

internal class VerifyOtpUseCaseImpl @Inject constructor(
    private val loginConnectRepository: LoginConnectRepository,
    private val authDataStore: AuthDataStore,
) : VerifyOtpUseCase {

    override suspend fun invoke(params: VerifyOtpUseCaseParams): Result<VerifyOtpResult> {
        return runCatching {
           val response = loginConnectRepository.verifyOtp(params.phoneNumber, params.otp)
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
        }.recover {
            VerifyOtpResult.Error(it.message.orEmpty())
        }
    }


}