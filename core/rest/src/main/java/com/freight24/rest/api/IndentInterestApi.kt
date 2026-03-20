package com.freight24.rest.api

import com.freight24.rest.response.IndentInterestInboxResponse
import retrofit2.Response
import retrofit2.http.GET

interface IndentInterestApi {

    @GET("indent-interests/inbox")
    suspend fun getIndentInterestInbox(): Response<IndentInterestInboxResponse>



}