package com.freight24.trucker.feature.login.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.trucker.feature.login.data.models.VerifyOtpResult

internal data class VerifyOtpUseCaseParams(
    val phoneNumber: String,
    val otp: String,
) : UseCaseParams

internal interface VerifyOtpUseCase : UseCase<VerifyOtpUseCaseParams, VerifyOtpResult>
