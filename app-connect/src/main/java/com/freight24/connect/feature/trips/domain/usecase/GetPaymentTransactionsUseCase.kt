package com.freight24.connect.feature.trips.domain.usecase

import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripRepository
import com.freight24.rest.response.PaymentTransaction
import javax.inject.Inject

data class GetPaymentTransactionsParams(
    val tripId: String,
    val userType: String,
) : UseCaseParams

internal interface GetPaymentTransactionsUseCase :
    UseCase<GetPaymentTransactionsParams, List<PaymentTransaction>>

class GetPaymentTransactionsUseCaseImpl
    @Inject
    constructor(
        private val repository: TripRepository,
    ) : GetPaymentTransactionsUseCase {
        override suspend fun invoke(params: GetPaymentTransactionsParams): Result<List<PaymentTransaction>> =
            repository.getPaymentTransactions(params.tripId, params.userType)
    }
