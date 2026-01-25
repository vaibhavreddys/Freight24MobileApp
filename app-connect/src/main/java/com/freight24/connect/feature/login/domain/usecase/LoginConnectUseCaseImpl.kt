package com.freight24.connect.feature.login.domain.usecase

import com.freight24.connect.feature.login.domain.model.LoginResult
import com.freight24.connect.feature.login.repository.LoginConnectRepository
import javax.inject.Inject

internal class LoginConnectUseCaseImpl @Inject constructor(
    private val loginConnectRepository: LoginConnectRepository,
) : LoginConnectUseCase {

    override suspend fun invoke(params: LoginConnectUseCaseParams): Result<LoginResult> {
        return runCatching {
            val response = loginConnectRepository.requestOtp(params.phoneNumber)
            LoginResult.Success
        }.recover { e ->
            LoginResult.Error(e.message ?: "Unknown error")
        }
    }

}