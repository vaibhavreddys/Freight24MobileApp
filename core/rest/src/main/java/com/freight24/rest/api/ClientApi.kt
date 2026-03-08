package com.freight24.rest.api

import com.freight24.rest.response.ClientListResponse
import com.freight24.rest.response.ClientResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ClientApi {
    @GET("client/list")
    suspend fun getClientList(): Response<ClientListResponse>

    @GET("client/search")
    suspend fun searchClients(
        @Query("q") query: String,
    ): Response<ClientResponse>
}
