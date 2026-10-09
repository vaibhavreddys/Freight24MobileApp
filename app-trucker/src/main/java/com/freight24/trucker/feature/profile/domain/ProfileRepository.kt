package com.freight24.trucker.feature.profile.domain

import com.freight24.rest.request.FinalSaveRequest
import java.io.File

interface ProfileRepository {
    suspend fun lookupIfsc(ifsc: String): Result<BankLookupDetails>

    suspend fun getProfile(): Result<Profile>

    suspend fun updateProfile(
        id: String,
        request: com.freight24.rest.request.UpdateProfileRequest,
    ): Result<Boolean>

    suspend fun uploadIdentityDocument(
        userId: String,
        documentTypeId: Int,
        documentNumber: String,
        file: okhttp3.MultipartBody.Part,
    ): Result<String>

    suspend fun updateIdentityDocument(
        docId: String,
        userId: String,
        documentNumber: String,
        file: okhttp3.MultipartBody.Part,
    ): Result<String>

    suspend fun finalizeIdentityDocuments(request: FinalSaveRequest): Result<Boolean>

    suspend fun discardTemporaryDocuments(userId: String, temporaryKeys: List<String>): Result<Boolean>

    suspend fun getIdentityDocumentSignedUrl(documentId: String): Result<String>

    suspend fun saveBankAccount(
        userId: String,
        accountId: String?,
        accountHolderName: String,
        accountNumber: String,
        ifscCode: String,
        upiId: String,
        bankName: String,
        branchName: String,
        chequeFile: File?,
    ): Result<Boolean>

    suspend fun getCancelledChequeSignedUrl(bankAccountId: String): Result<String>
}

data class BankLookupDetails(
    val bankName: String,
    val branchName: String,
)
