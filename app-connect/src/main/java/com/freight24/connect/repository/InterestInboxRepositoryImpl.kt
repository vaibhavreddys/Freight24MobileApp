package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.IndentInterestApi
import com.freight24.rest.response.IndentInterestInboxResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InterestInboxRepositoryImpl @Inject constructor(
    private val api: IndentInterestApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : InterestInboxRepository {
    override suspend fun getInbox(): Result<IndentInterestInboxResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.getIndentInterestInbox()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}