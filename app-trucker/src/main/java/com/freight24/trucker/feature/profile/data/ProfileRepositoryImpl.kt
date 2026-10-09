package com.freight24.trucker.feature.profile.data

import com.freight24.common.di.IoDispatcher
import com.freight24.network.provider.SafeCallProvider
import com.freight24.rest.request.FinalSaveRequest
import com.freight24.rest.request.DiscardTemporaryDocumentsRequest
import com.freight24.rest.response.IdentityDocumentUploadResponse
import com.freight24.trucker.feature.profile.domain.Profile
import com.freight24.trucker.feature.profile.domain.ProfileRepository
import com.freight24.trucker.feature.profile.domain.BankLookupDetails
import com.freight24.trucker.feature.profile.mapper.toDomain
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MultipartBody
import java.io.File
import javax.inject.Inject

internal class ProfileRepositoryImpl
    @Inject
    constructor(
        private val api: com.freight24.rest.api.ProfileApi,
        private val documentApi: com.freight24.rest.api.DocumentApi,
        private val safeCallProvider: SafeCallProvider,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ProfileRepository {
        override suspend fun lookupIfsc(ifsc: String): Result<BankLookupDetails> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                api.validateIfsc(ifsc)
            }.map { response ->
                val bank = response.data?.takeIf { it.valid }?.bank
                    ?: throw IllegalArgumentException("Enter a valid IFSC code")
                BankLookupDetails(bankName = bank.name, branchName = bank.branch)
            }
        }

        override suspend fun getProfile(): Result<Profile> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                api.getProfile()
            }.map { response ->
                response.data?.toDomain() ?: throw IllegalStateException("Profile data is null")
            }
        }

        override suspend fun updateProfile(
            id: String,
            request: com.freight24.rest.request.UpdateProfileRequest,
        ): Result<Boolean> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                api.updateProfile(id, request)
            }.map { it.success }
        }

        override suspend fun uploadIdentityDocument(
            userId: String,
            documentTypeId: Int,
            documentNumber: String,
            file: okhttp3.MultipartBody.Part,
        ): Result<String> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                val documentNumberBody = documentNumber.toRequestBody("text/plain".toMediaTypeOrNull())
                api.uploadIdentityDocumentWithTemporaryKey(file, documentTypeId, userId, documentNumberBody)
            }.map { response ->
                response.extractTemporaryFileKey()
            }
        }

        override suspend fun updateIdentityDocument(
            docId: String,
            userId: String,
            documentNumber: String,
            file: okhttp3.MultipartBody.Part,
        ): Result<String> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                val documentNumberBody = documentNumber.toRequestBody("text/plain".toMediaTypeOrNull())
                api.updateIdentityDocumentWithTemporaryKey(docId, userId, file, documentNumberBody)
            }.map { response ->
                response.extractTemporaryFileKey()
            }
        }

        override suspend fun finalizeIdentityDocuments(request: FinalSaveRequest): Result<Boolean> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                api.finalizeIdentityDocuments(request)
            }.map { it.success }
        }

        override suspend fun discardTemporaryDocuments(userId: String, temporaryKeys: List<String>): Result<Boolean> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                api.discardTemporaryDocuments(DiscardTemporaryDocumentsRequest(userId, temporaryKeys))
            }.map { it.success }
        }

        override suspend fun getIdentityDocumentSignedUrl(documentId: String): Result<String> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                documentApi.getIdentityDocumentSignedUrl(documentId)
            }.map { it.data.signedUrl }
        }

        override suspend fun saveBankAccount(
            userId: String,
            accountId: String?,
            accountHolderName: String,
            accountNumber: String,
            ifscCode: String,
            upiId: String,
            bankName: String,
            branchName: String,
            chequeFile: File?,
        ): Result<Boolean> {
            val parts = mutableListOf(
                textPart("user_id", userId),
                textPart("account_holder_name", accountHolderName),
                textPart("account_number", accountNumber),
                textPart("ifsc_code", ifscCode),
                textPart("upi_id", upiId),
                textPart("bank_name", bankName),
                textPart("branch_name", branchName),
                textPart("is_primary", "true"),
            )
            chequeFile?.let { file ->
                parts += MultipartBody.Part.createFormData(
                    "cheque_file",
                    file.name,
                    file.asRequestBody("application/pdf".toMediaTypeOrNull()),
                )
            }
            return safeCallProvider.safeApiCall(ioDispatcher) {
                if (accountId.isNullOrBlank()) {
                    api.addBankAccount(parts)
                } else {
                    api.updateBankAccount(accountId, userId, parts)
                }
            }.map { it.success }
        }

        override suspend fun getCancelledChequeSignedUrl(bankAccountId: String): Result<String> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                documentApi.getCancelledChequeSignedUrl(bankAccountId)
            }.map { it.data.signedUrl }
        }

        private fun textPart(name: String, value: String): MultipartBody.Part =
            MultipartBody.Part.createFormData(name, value)

        private fun IdentityDocumentUploadResponse.extractTemporaryFileKey(): String {
            val key = data?.temporaryFileKey ?: data?.fileKey
            return key ?: throw IllegalStateException("temporaryFileKey missing in upload response")
        }
    }
