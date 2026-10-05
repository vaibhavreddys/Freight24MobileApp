package com.freight24.connect.feature.truckowners.profile.data

import com.freight24.common.di.IoDispatcher
import com.freight24.connect.feature.truckowners.profile.domain.Profile
import com.freight24.connect.feature.truckowners.profile.domain.ProfileRepository
import com.freight24.connect.feature.truckowners.profile.mapper.toDomain
import com.freight24.network.provider.SafeCallProvider
import com.freight24.rest.api.ProfileApi
import com.freight24.rest.api.TruckOwnerApi
import com.freight24.rest.request.FinalSaveRequest
import com.freight24.rest.request.UpdateTruckOwnerStatusRequest
import com.freight24.rest.response.IdentityDocumentUploadResponse
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

internal class ProfileRepositoryImpl
    @Inject
    constructor(
        private val truckOwnerApi: TruckOwnerApi,
        private val api: ProfileApi,
        private val documentApi: com.freight24.rest.api.DocumentApi,
        private val safeCallProvider: SafeCallProvider,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ProfileRepository {
        override suspend fun getProfile(userId: String): Result<Profile> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                truckOwnerApi.getTruckOwnerProfile(userId)
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

        override suspend fun updateTruckOwnerStatus(
            userId: String,
            status: String,
        ): Result<Boolean> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                truckOwnerApi.updateTruckOwnerStatus(userId, UpdateTruckOwnerStatusRequest(status))
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

        override suspend fun getIdentityDocumentSignedUrl(documentId: String): Result<String> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                documentApi.getIdentityDocumentSignedUrl(documentId)
            }.map { it.data.signedUrl }
        }

        private fun IdentityDocumentUploadResponse.extractTemporaryFileKey(): String {
            val key = data?.temporaryFileKey ?: data?.fileKey
            return key ?: throw IllegalStateException("temporaryFileKey missing in upload response")
        }
    }
