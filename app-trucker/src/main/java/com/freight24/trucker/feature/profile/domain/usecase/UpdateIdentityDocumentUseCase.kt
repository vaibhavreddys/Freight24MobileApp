package com.freight24.trucker.feature.profile.domain.usecase

import com.freight24.trucker.feature.profile.domain.ProfileRepository
import okhttp3.MultipartBody
import javax.inject.Inject

interface UpdateIdentityDocumentUseCase {
    suspend operator fun invoke(params: UpdateIdentityDocumentParams): Result<String>
}

data class UpdateIdentityDocumentParams(
    val docId: String,
    val userId: String,
    val documentNumber: String,
    val file: MultipartBody.Part,
)

class UpdateIdentityDocumentUseCaseImpl
    @Inject
    constructor(
        private val repository: ProfileRepository,
    ) : UpdateIdentityDocumentUseCase {
        override suspend fun invoke(params: UpdateIdentityDocumentParams): Result<String> {
            return repository.updateIdentityDocument(params.docId, params.userId, params.documentNumber, params.file)
        }
    }
