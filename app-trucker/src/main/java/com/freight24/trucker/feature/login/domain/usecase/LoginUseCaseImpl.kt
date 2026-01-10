package com.freight24.trucker.feature.login.domain.usecase

import com.freight24.trucker.feature.login.data.models.LoginResult
import com.freight24.trucker.feature.login.data.repository.LoginRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LoginUseCaseImpl
    @Inject
    constructor(
        private val loginRepository: LoginRepository,
    ) : LoginUseCase {
        override suspend fun invoke(params: LoginUseCaseParams): Result<LoginResult> =
            runCatching {
                val response = loginRepository.requestOtp(params.phoneNumber)
                val otp = response.data?.otp_code.orEmpty()
                LoginResult.Success(otp)
            }.recover { e ->
                LoginResult.Error(e.message.orEmpty())
            }
    }
