package com.freight24.connect.repository

import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.IndentInterestInboxResponse
import com.freight24.rest.response.TruckerInboxInterestResponse

interface InterestInboxRepository {
    suspend fun getInbox(indentId: String?): Result<IndentInterestInboxResponse>

    suspend fun withdrawInterest(
        interestId: String,
        remarks: String,
    ): Result<BaseResponse>

    suspend fun getTruckerInterestInbox(indentId: String?): Result<TruckerInboxInterestResponse>
}
