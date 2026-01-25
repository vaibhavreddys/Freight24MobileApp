package com.freight24.trucker.feature.profile.data

import com.freight24.common.di.IoDispatcher
import com.freight24.network.provider.SafeCallProvider
import com.freight24.rest.api.ProfileApi
import com.freight24.trucker.feature.profile.domain.Profile
import com.freight24.trucker.feature.profile.domain.ProfileRepository
import com.freight24.trucker.feature.profile.mapper.toDomain
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

internal class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    private val safeCallProvider: SafeCallProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ProfileRepository {
    override suspend fun getProfile(): Result<Profile> {
        return safeCallProvider.safeApiCall(ioDispatcher) {
            api.getProfile()
        }.map { response ->
            response.data?.toDomain() ?: throw IllegalStateException("Profile data is null")
        }
    }
}
