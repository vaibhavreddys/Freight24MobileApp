package com.freight24.connect.feature.login.domain.usecase

import com.freight24.connect.feature.login.repository.LoginConnectRepository
import javax.inject.Inject

internal class LoginConnectUseCaseImpl
    @Inject
    constructor(
        private val loginConnectRepository: LoginConnectRepository,
    ) : LoginConnectUseCase {
        override suspend fun invoke(params: LoginConnectUseCaseParams): Result<Unit> {
            return loginConnectRepository.requestOtp(params.phoneNumber)
        }
    }
