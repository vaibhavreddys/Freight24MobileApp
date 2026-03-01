package com.freight24.trucker.feature.trucks.domain.usecase

import com.freight24.trucker.feature.trucks.data.repository.TruckRepository
import javax.inject.Inject

internal class GetGenericSignedUrlUseCaseImpl @Inject constructor(
    private val repository: TruckRepository
) : GetGenericSignedUrlUseCase {
    override suspend fun invoke(params: GetGenericSignedUrlParams): Result<String> {
        return repository.getGenericSignedUrl(params.s3Url)
    }
}
