package com.freight24.rest.api

import com.freight24.rest.request.UpdateClientRequest
import com.freight24.rest.response.ClientListResponse
import com.freight24.rest.response.ClientProfileResponse
import com.freight24.rest.response.ClientResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ClientApi {
    @GET("client/list")
    suspend fun getClientList(): Response<ClientListResponse>

    @GET("client/search")
    suspend fun searchClients(
        @Query("q") query: String,
    ): Response<ClientResponse>

    @GET("client/{userId}")
    suspend fun getClientByUserId(
        @Path("userId") userId: String,
    ): Response<ClientProfileResponse>

    @PUT("client/{userId}")
    suspend fun updateClient(
        @Path("userId") userId: String,
        @Body request: UpdateClientRequest,
    ): Response<ClientProfileResponse>
}
