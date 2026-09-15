package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.connect.repository.ClientRepository
import com.freight24.rest.response.ClientBranchV2Dto
import javax.inject.Inject

/** Loads the branches of the selected client for the indent's branch picker. */
interface GetClientBranchesUseCase {
    suspend fun invoke(clientId: String): Result<List<ClientBranchV2Dto>>
}

class GetClientBranchesUseCaseImpl
    @Inject
    constructor(
        private val repository: ClientRepository,
    ) : GetClientBranchesUseCase {
        override suspend fun invoke(clientId: String): Result<List<ClientBranchV2Dto>> =
            repository.getClientBranches(clientId)
    }
