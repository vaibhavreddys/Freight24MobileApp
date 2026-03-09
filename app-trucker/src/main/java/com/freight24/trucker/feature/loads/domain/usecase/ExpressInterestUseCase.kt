package com.freight24.trucker.feature.loads.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.rest.response.IndentInterestResponse

data class ExpressInterestUseCaseParams(
    val indentId: String,
) : UseCaseParams

interface ExpressInterestUseCase : UseCase<ExpressInterestUseCaseParams, IndentInterestResponse>
