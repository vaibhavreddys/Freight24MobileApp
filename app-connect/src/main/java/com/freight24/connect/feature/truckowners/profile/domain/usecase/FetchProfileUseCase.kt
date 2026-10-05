package com.freight24.connect.feature.truckowners.profile.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.feature.truckowners.profile.domain.Profile

data class FetchProfileParams(
    val userId: String,
) : UseCaseParams

interface FetchProfileUseCase : UseCase<FetchProfileParams, Profile>
