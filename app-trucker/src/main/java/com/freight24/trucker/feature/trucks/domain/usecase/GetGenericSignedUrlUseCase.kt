package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams

data class GetGenericSignedUrlParams(val s3Url: String) : UseCaseParams

interface GetGenericSignedUrlUseCase : UseCase<GetGenericSignedUrlParams, String>
