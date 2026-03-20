package com.freight24.connect.repository

import com.freight24.rest.response.IndentInterestInboxResponse

interface InterestInboxRepository {

    suspend fun getInbox(): Result<IndentInterestInboxResponse>

}