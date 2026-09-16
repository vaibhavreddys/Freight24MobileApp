package com.freight24.connect.feature.indents.domain.usecases

import com.freight24.connect.repository.IndentRepository
import com.freight24.rest.response.MaterialDto
import javax.inject.Inject

/**
 * The cargo categories offered for an indent's material.
 *
 * Loaded once when the create screen opens rather than on sheet open: the list
 * is served from a backend constant today, but when it moves into a table the
 * sheet would otherwise stall on the first tap.
 */
interface GetMaterialsUseCase {
    suspend fun invoke(): Result<List<MaterialDto>>
}

class GetMaterialsUseCaseImpl
    @Inject
    constructor(
        private val repository: IndentRepository,
    ) : GetMaterialsUseCase {
        override suspend fun invoke(): Result<List<MaterialDto>> {
            return repository.getMaterials()
        }
    }
