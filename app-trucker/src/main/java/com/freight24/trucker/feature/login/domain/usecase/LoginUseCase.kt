package com.freight24.trucker.feature.login.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.trucker.feature.login.data.models.LoginResult

internal data class LoginUseCaseParams(
    val phoneNumber: String,
) : UseCaseParams

internal interface LoginUseCase : UseCase<LoginUseCaseParams, LoginResult>
