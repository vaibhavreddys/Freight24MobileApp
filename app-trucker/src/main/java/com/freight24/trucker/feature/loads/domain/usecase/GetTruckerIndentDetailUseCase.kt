package com.freight24.trucker.feature.loads.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.rest.response.TruckerIndentDetailResponse

data class GetTruckerIndentDetailParams(
    val indentId: String,
) : UseCaseParams

interface GetTruckerIndentDetailUseCase : UseCase<GetTruckerIndentDetailParams, TruckerIndentDetailResponse>
