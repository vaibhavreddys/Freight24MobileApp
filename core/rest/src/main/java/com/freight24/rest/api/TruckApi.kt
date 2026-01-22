package com.freight24.rest.api

import com.freight24.rest.response.TruckResponse
import retrofit2.Response
import retrofit2.http.GET

interface TruckApi {
    @GET("trucks")
    suspend fun getTrucks(): Response<TruckResponse>
}
