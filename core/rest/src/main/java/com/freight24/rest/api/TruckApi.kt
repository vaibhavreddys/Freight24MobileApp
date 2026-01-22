package com.freight24.rest.api

import com.freight24.rest.response.TruckDetailResponse
import com.freight24.rest.response.TruckResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TruckApi {
    @GET("trucks")
    suspend fun getTrucks(): Response<TruckResponse>

    @GET("trucks/{id}")
    suspend fun getTruckDetail(
        @Path("id") id: String
    ): Response<TruckDetailResponse>
}
