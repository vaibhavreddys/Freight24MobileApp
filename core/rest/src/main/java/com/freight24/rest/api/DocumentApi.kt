package com.freight24.rest.api

import com.freight24.rest.response.GenericSignedUrlResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DocumentApi {
    @GET("documents/signed-url")
    suspend fun getGenericSignedUrl(
        @Query("s3Url") s3Url: String
    ): Response<GenericSignedUrlResponse>
}
