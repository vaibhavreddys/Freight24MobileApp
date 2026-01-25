package com.freight24.connect.feature.login.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.feature.login.domain.model.LoginResult

data class LoginConnectUseCaseParams(val phoneNumber: String) : UseCaseParams

internal interface LoginConnectUseCase : UseCase<LoginConnectUseCaseParams, LoginResult>