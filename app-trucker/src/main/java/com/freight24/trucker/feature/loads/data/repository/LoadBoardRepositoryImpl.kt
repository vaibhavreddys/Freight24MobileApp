package com.freight24.trucker.feature.loads.data.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.database.dao.LoadBoardDao
import com.freight24.network.cache.LoadMemoryCache
import com.freight24.network.provider.SafeCallProvider
import com.freight24.rest.api.IndentsApi
import com.freight24.rest.request.IndentInterestRequest
import com.freight24.rest.response.IndentInterestResponse
import com.freight24.rest.response.LoadBoardResponse
import com.freight24.rest.response.PaginatedLoadBoardResponse
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class LoadBoardRepositoryImpl
    @Inject
    constructor(
        private val safeCallProvider: SafeCallProvider,
        private val indentsApi: IndentsApi,
        private val dao: LoadBoardDao,
        private val memoryCache: LoadMemoryCache,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : LoadBoardRepository {
        override suspend fun fetchLoadBoardSummary(forceRefresh: Boolean): Result<LoadBoardResponse> =
            runCatching {
                safeCallProvider.safeApiCall(ioDispatcher) {
                    indentsApi.getLoadBoardSummary()
                }.getOrThrow()
            }

        override suspend fun fetchLoadsForCity(
            cityId: Int,
            page: Int,
            forceRefresh: Boolean,
        ): Result<PaginatedLoadBoardResponse> =
            runCatching {
                safeCallProvider.safeApiCall(ioDispatcher) {
                    indentsApi.getPaginatedLoads(cityId, page)
                }.getOrThrow()
            }

        override suspend fun expressInterest(indentId: String): Result<IndentInterestResponse> =
            runCatching {
                safeCallProvider.safeApiCall(ioDispatcher) {
                    indentsApi.expressInterest(IndentInterestRequest(indentId))
                }.getOrThrow()
            }
    }
