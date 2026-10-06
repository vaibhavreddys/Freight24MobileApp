package com.freight24.connect.feature.trucks.domain.usecases

import com.freight24.connect.domain.model.EntityPage
import com.freight24.connect.domain.model.TruckOwnerCounts
import com.freight24.connect.domain.model.TruckOwnerManagementItem
import com.freight24.connect.domain.model.TruckOwnerPickerOption
import com.freight24.connect.domain.model.TruckOwnerValidation
import com.freight24.connect.repository.TruckOwnerRepository
import javax.inject.Inject

data class GetTruckOwnersParams(
    val query: String? = null,
    val status: String? = null,
    val documentStatus: String? = null,
    val page: Int = 1,
    val limit: Int = 30,
)

interface GetTruckOwnersUseCase {
    suspend operator fun invoke(params: GetTruckOwnersParams = GetTruckOwnersParams()):
        Result<EntityPage<TruckOwnerManagementItem, TruckOwnerCounts>>
}

class GetTruckOwnersUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckOwnerRepository,
    ) : GetTruckOwnersUseCase {
        override suspend fun invoke(params: GetTruckOwnersParams) =
            repository.getManagementPage(
                query = params.query,
                status = params.status,
                documentStatus = params.documentStatus,
                page = params.page,
                limit = params.limit,
            )
    }

interface SearchTruckOwnerOptionsUseCase {
    suspend operator fun invoke(
        query: String,
        page: Int = 1,
        limit: Int = 30,
    ): Result<EntityPage<TruckOwnerPickerOption, TruckOwnerCounts>>
}

class SearchTruckOwnerOptionsUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckOwnerRepository,
    ) : SearchTruckOwnerOptionsUseCase {
        override suspend fun invoke(query: String, page: Int, limit: Int) =
            repository.searchPickerOptions(query, page, limit)
    }

data class ValidateTruckOwnerParams(
    val phone: String? = null,
    val pan: String? = null,
    val aadhaar: String? = null,
    val gst: String? = null,
    val excludeOwnerUserId: String? = null,
)

interface ValidateTruckOwnerUseCase {
    suspend operator fun invoke(params: ValidateTruckOwnerParams): Result<TruckOwnerValidation>
}

class ValidateTruckOwnerUseCaseImpl
    @Inject
    constructor(
        private val repository: TruckOwnerRepository,
    ) : ValidateTruckOwnerUseCase {
        override suspend fun invoke(params: ValidateTruckOwnerParams) =
            repository.validate(
                phone = params.phone,
                pan = params.pan,
                aadhaar = params.aadhaar,
                gst = params.gst,
                excludeOwnerUserId = params.excludeOwnerUserId,
            )
    }
