package com.freight24.connect.feature.verificationrequests.domain.di

import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationRequestsUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationRequestsUseCaseImpl
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationSignedUrlUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationSignedUrlUseCaseImpl
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationTruckRequestsUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.GetVerificationTruckRequestsUseCaseImpl
import com.freight24.connect.feature.verificationrequests.domain.usecase.UpdateTruckVerificationStatusUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.UpdateTruckVerificationStatusUseCaseImpl
import com.freight24.connect.feature.verificationrequests.domain.usecase.UpdateVerificationRequestStatusUseCase
import com.freight24.connect.feature.verificationrequests.domain.usecase.UpdateVerificationRequestStatusUseCaseImpl
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
    abstract fun bindGetVerificationTruckRequestsUseCase(
        useCaseImpl: GetVerificationTruckRequestsUseCaseImpl
    ): GetVerificationTruckRequestsUseCase

    @Binds
    abstract fun bindGetVerificationSignedUrlUseCase(
        useCaseImpl: GetVerificationSignedUrlUseCaseImpl
    ): GetVerificationSignedUrlUseCase

    @Binds
    abstract fun bindUpdateVerificationRequestStatusUseCase(
        useCaseImpl: UpdateVerificationRequestStatusUseCaseImpl
    ): UpdateVerificationRequestStatusUseCase

    @Binds
    abstract fun bindUpdateTruckVerificationStatusUseCase(
        useCaseImpl: UpdateTruckVerificationStatusUseCaseImpl
    ): UpdateTruckVerificationStatusUseCase
}
