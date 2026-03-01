package com.freight24.connect.feature.trips.domain.di

import com.freight24.connect.feature.trips.domain.usecase.CreateTripUseCase
import com.freight24.connect.feature.trips.domain.usecase.CreateTripUseCaseImpl
import com.freight24.connect.feature.trips.domain.usecase.GetHaltingRateUseCase
import com.freight24.connect.feature.trips.domain.usecase.GetHaltingRateUseCaseImpl
import com.freight24.connect.feature.trips.domain.usecase.GetMyTripsUseCase
import com.freight24.connect.feature.trips.domain.usecase.GetMyTripsUseCaseImpl
import com.freight24.connect.feature.trips.domain.usecase.SignedDocumentUseCase
import com.freight24.connect.feature.trips.domain.usecase.SignedDocumentUseCaseImpl
import com.freight24.connect.feature.trips.domain.usecase.SlipUrlSignedDocumentUseCase
import com.freight24.connect.feature.trips.domain.usecase.SlipUrlSignedDocumentUseCaseImpl
import com.freight24.connect.feature.trips.domain.usecase.TripDetailsUseCase
import com.freight24.connect.feature.trips.domain.usecase.TripDetailsUseCaseImpl
import com.freight24.connect.feature.trips.domain.usecase.UpdateHaltingRateUseCase
import com.freight24.connect.feature.trips.domain.usecase.UpdateHaltingRateUseCaseImpl
import com.freight24.connect.feature.trips.domain.usecase.UpdateOtherDeductionsUseCase
import com.freight24.connect.feature.trips.domain.usecase.UpdateOtherDeductionsUseCaseImpl
import com.freight24.connect.feature.trips.domain.usecase.UpdateTripUseCase
import com.freight24.connect.feature.trips.domain.usecase.UpdateTripUseCaseImpl
import com.freight24.connect.feature.trips.domain.usecase.UpdateTruckerUseCase
import com.freight24.connect.feature.trips.domain.usecase.UpdateTruckerUseCaseImpl
import com.freight24.connect.feature.trips.domain.usecase.UploadLUChargesUseCase
import com.freight24.connect.feature.trips.domain.usecase.UploadLUChargesUseCaseImpl
import com.freight24.connect.feature.trips.domain.usecase.UploadLrUseCase
import com.freight24.connect.feature.trips.domain.usecase.UploadLrUseCaseImpl
import com.freight24.connect.feature.trips.domain.usecase.VerifyLrUseCase
import com.freight24.connect.feature.trips.domain.usecase.VerifyLrUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TripsDomainModule {

    @Binds
    abstract fun bindCreateTripUseCase(useCaseImpl: CreateTripUseCaseImpl): CreateTripUseCase

    @Binds
    abstract fun bindGetMyTripsUseCase(useCaseImpl: GetMyTripsUseCaseImpl): GetMyTripsUseCase

    @Binds
    abstract fun bindTripDetailsUseCase(useCaseImpl: TripDetailsUseCaseImpl): TripDetailsUseCase

    @Binds
    abstract fun bindUpdateTruckerUseCase(useCaseImpl: UpdateTruckerUseCaseImpl): UpdateTruckerUseCase

    @Binds
    abstract fun bindUploadLrUseCase(useCaseImpl: UploadLrUseCaseImpl): UploadLrUseCase

    @Binds
    abstract fun bindSignedDocumentUseCase(useCaseImpl: SignedDocumentUseCaseImpl): SignedDocumentUseCase

    @Binds
    abstract fun bindVerifyLrUseCase(useCaseImpl: VerifyLrUseCaseImpl): VerifyLrUseCase

    @Binds
    abstract fun bindGetHaltingRateUseCase(useCaseImpl: GetHaltingRateUseCaseImpl): GetHaltingRateUseCase

    @Binds
    abstract fun bindUpdateHaltingRateUseCase(useCaseImpl: UpdateHaltingRateUseCaseImpl): UpdateHaltingRateUseCase

    @Binds
    abstract fun bindUploadLUChargesUseCase(useCaseImpl: UploadLUChargesUseCaseImpl): UploadLUChargesUseCase

    @Binds
    abstract fun bindSlipUrlSignedDocumentUseCase(useCaseImpl: SlipUrlSignedDocumentUseCaseImpl): SlipUrlSignedDocumentUseCase

    @Binds
    abstract fun bindUpdateOtherDeductionsUseCase(useCaseImpl: UpdateOtherDeductionsUseCaseImpl): UpdateOtherDeductionsUseCase

    @Binds
    abstract fun bindUpdateTripUseCase(useCaseImpl: UpdateTripUseCaseImpl): UpdateTripUseCase

}