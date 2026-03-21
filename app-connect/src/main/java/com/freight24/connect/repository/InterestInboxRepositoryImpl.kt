package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.rest.api.IndentInterestApi
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.IndentInterestInboxResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

import com.freight24.rest.request.WithdrawInterestRequest
import com.freight24.rest.response.TruckerInboxInterestResponse

class InterestInboxRepositoryImpl @Inject constructor(
    private val api: IndentInterestApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : InterestInboxRepository {

    override suspend fun getInbox(indentId: String?): Result<IndentInterestInboxResponse> =
        withContext(ioDispatcher) {
            try {
                val response = api.getIndentInterestInbox(indentId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun withdrawInterest(
        interestId: String,
        remarks: String
    ): Result<BaseResponse> =
        withContext(ioDispatcher) {
            try {
                val request = WithdrawInterestRequest(
                    interestId = interestId,
                    remarks = remarks
                )
                val response = api.withdrawInterest(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getTruckerInterestInbox(indentId: String?) =
        withContext(ioDispatcher) {
            try {
                val response = api.getTruckersInterestInbox(indentId)
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