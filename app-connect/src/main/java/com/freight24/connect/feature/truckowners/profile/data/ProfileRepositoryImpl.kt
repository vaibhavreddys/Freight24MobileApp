package com.freight24.connect.feature.truckowners.profile.data

import com.freight24.common.di.IoDispatcher
import com.freight24.connect.feature.truckowners.profile.domain.Profile
import com.freight24.connect.feature.truckowners.profile.domain.ProfileRepository
import com.freight24.connect.feature.truckowners.profile.mapper.toProfile
import com.freight24.connect.repository.TruckOwnerRepository
import com.freight24.network.provider.SafeCallProvider
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

internal class ProfileRepositoryImpl
    @Inject
    constructor(
        private val truckOwnerRepository: TruckOwnerRepository,
        private val documentApi: com.freight24.rest.api.DocumentApi,
        private val safeCallProvider: SafeCallProvider,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ProfileRepository {
        override suspend fun getProfile(userId: String): Result<Profile> {
            return truckOwnerRepository.getDetails(userId).map { it.toProfile() }
        }

        override suspend fun updateTruckOwnerStatus(
            userId: String,
            status: String,
        ): Result<Boolean> {
            return truckOwnerRepository.updateStatus(userId, status).map { true }
        }

        override suspend fun getIdentityDocumentSignedUrl(documentId: String): Result<String> {
            return safeCallProvider.safeApiCall(ioDispatcher) {
                documentApi.getIdentityDocumentSignedUrl(documentId)
            }.map { it.data.signedUrl }
        }
    }
