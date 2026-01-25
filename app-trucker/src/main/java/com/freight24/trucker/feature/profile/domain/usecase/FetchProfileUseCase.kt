package com.freight24.trucker.feature.profile.domain.usecase

import com.freight24.common.usecase.EmptyUseCaseParams
import com.freight24.common.usecase.UseCase
import com.freight24.trucker.feature.profile.domain.Profile

interface FetchProfileUseCase : UseCase<EmptyUseCaseParams, Profile>
