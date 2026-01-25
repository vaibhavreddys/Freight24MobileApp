package com.freight24.rest.api

import com.freight24.rest.response.ProfileResponse
import retrofit2.Response
import retrofit2.http.GET

interface ProfileApi {
    @GET("truck-owners/profile")
    suspend fun getProfile(): Response<ProfileResponse>
}
