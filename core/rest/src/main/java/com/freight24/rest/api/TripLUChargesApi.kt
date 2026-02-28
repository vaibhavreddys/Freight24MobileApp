package com.freight24.rest.api

import com.freight24.rest.response.UpdatedLUResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

interface TripLUChargesApi {

    @Multipart
    @POST("trips/{tripId}/lu-charge/bulk")
    suspend fun updateLUCharges(
        @Path("tripId") tripId: String,
        @Part loadingFile: MultipartBody.Part?,
        @Part unloadingFile: MultipartBody.Part?,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
    ): Response<UpdatedLUResponse>

}