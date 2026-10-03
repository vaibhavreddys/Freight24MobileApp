package com.freight24.trucker.feature.login.domain.usecase

import com.freight24.trucker.feature.login.data.models.LoginResult
import com.freight24.trucker.feature.login.data.repository.LoginRepository
import com.freight24.trucker.feature.login.domain.AuthErrorMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LoginUseCaseImpl
    @Inject
    constructor(
        private val loginRepository: LoginRepository,
    ) : LoginUseCase {
        // `runCatching` swallows the throwable into a *successful*
        // `LoginResult.Error` rather than a failed `Result` — callers must
        // therefore branch on the sealed type, not on Result.isFailure. See
        // LoginViewModel.onSendOtpClick, which got this wrong and left the
        // Send-code button spinning forever on a dead network.
        override suspend fun invoke(params: LoginUseCaseParams): Result<LoginResult> =
            runCatching {
                val response = loginRepository.requestOtp(params.phoneNumber)
                val otp = response.data?.otp_code.orEmpty()
                LoginResult.Success(otp)
            }.recover { e ->
                // e.message is an OkHttp string like "Failed to connect to
                // 10.0.2.2:3000" — never shown to the driver.
                LoginResult.Error(AuthErrorMessage.from(e))
            }
    }
