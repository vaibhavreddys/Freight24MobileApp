package com.freight24.connect.feature.verificationrequests.domain.di

import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationRequestsUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationRequestsUseCaseImpl
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationSignedUrlUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationSignedUrlUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class VerificationRequestsDomainModule {
    @Binds
    abstract fun bindGetVerificationRequestsUseCase(
        useCaseImpl: GetVerificationRequestsUseCaseImpl
    ): GetVerificationRequestsUseCase

    @Binds
    abstract fun bindGetVerificationSignedUrlUseCase(
        useCaseImpl: GetVerificationSignedUrlUseCaseImpl
    ): GetVerificationSignedUrlUseCase
}
