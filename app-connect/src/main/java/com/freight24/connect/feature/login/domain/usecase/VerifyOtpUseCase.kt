package com.freight24.connect.feature.login.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.feature.login.domain.model.VerifyOtpResult


data class VerifyOtpUseCaseParams(val phoneNumber: String, val otp: String): UseCaseParams

interface VerifyOtpUseCase : UseCase<VerifyOtpUseCaseParams, VerifyOtpResult>