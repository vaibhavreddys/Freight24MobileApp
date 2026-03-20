package com.freight24.trucker.feature.profile.domain.usecase

import com.freight24.trucker.feature.profile.domain.ProfileRepository
import okhttp3.MultipartBody
import javax.inject.Inject

interface UploadIdentityDocumentUseCase {
    suspend operator fun invoke(params: UploadIdentityDocumentParams): Result<String>
}

data class UploadIdentityDocumentParams(
    val userId: String,
    val documentTypeId: Int,
    val documentNumber: String,
    val file: MultipartBody.Part
)

class UploadIdentityDocumentUseCaseImpl @Inject constructor(
    private val repository: ProfileRepository
) : UploadIdentityDocumentUseCase {
    override suspend fun invoke(params: UploadIdentityDocumentParams): Result<String> {
        return repository.uploadIdentityDocument(params.userId, params.documentTypeId, params.documentNumber, params.file)
    }
}
