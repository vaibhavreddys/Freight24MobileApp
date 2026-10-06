package com.freight24.connect.repository

import com.freight24.common.di.IoDispatcher
import com.freight24.network.error.toApiException
import com.freight24.rest.api.TruckOwnerApi
import com.freight24.rest.api.TruckOwnerV2Api
import com.freight24.rest.response.TruckOwnerOnboardingV2Response
import com.freight24.rest.response.TruckOwnerDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

class TruckOwnerRepositoryImpl
    @Inject
    constructor(
        private val api: TruckOwnerApi,
        private val v2Api: TruckOwnerV2Api,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : TruckOwnerRepository {
        override suspend fun getTruckOwners(): Result<List<TruckOwnerDto>> =
            withContext(ioDispatcher) {
                try {
                    val response = api.getTruckOwners()
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.data)
                    } else {
                        Result.failure(response.toApiException(bodyMessage = response.body()?.message))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun searchTruckOwners(query: String): Result<List<TruckOwnerDto>> =
            withContext(ioDispatcher) {
                try {
                    val response = api.searchTruckOwners(query)
                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!.data)
                    } else {
                        Result.failure(response.toApiException(bodyMessage = response.body()?.message))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun createPersonalProfile(parts: List<MultipartBody.Part>) =
            executeOnboardingRequest {
                v2Api.createPersonalProfile(
                    parts.required("full_name"),
                    parts.optional("email"),
                    parts.required("phone"),
                    parts.optional("alt_phones"),
                    parts.required("pan_card"),
                    parts.required("aadhar_card"),
                    parts.required("aadhaar_file"),
                    parts.required("pan_file"),
                )
            }

        override suspend fun updateBusinessProfile(
            ownerUserId: String,
            parts: List<MultipartBody.Part>,
        ) = executeOnboardingRequest {
            v2Api.updateBusinessProfile(
                ownerUserId,
                parts.optional("business_name"),
                parts.optional("gst_number"),
                parts.required("is_tds_applicable"),
                parts.optional("base_city_id"),
                parts.required("preferred_routes"),
                parts.optional("gst_file"),
                parts.optional("company_file"),
                parts.optional("tds_file"),
            )
        }

        override suspend fun updateBankProfile(
            ownerUserId: String,
            parts: List<MultipartBody.Part>,
        ) = executeOnboardingRequest {
            v2Api.updateBankProfile(
                ownerUserId,
                parts.required("account_holder_name"),
                parts.required("account_number"),
                parts.required("ifsc_code"),
                parts.optional("upi_id"),
                parts.optional("bank_name"),
                parts.optional("branch_name"),
                parts.required("cheque_file"),
            )
        }

        private suspend fun executeOnboardingRequest(
            request: suspend () -> Response<TruckOwnerOnboardingV2Response>,
        ): Result<TruckOwnerOnboardingV2Response> =
            withContext(ioDispatcher) {
                try {
                    val response = request()
                    val body = response.body()
                    if (response.isSuccessful && body != null && body.success) {
                        Result.success(body)
                    } else {
                        Result.failure(response.toApiException(bodyMessage = body?.message))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        private fun List<MultipartBody.Part>.required(name: String): MultipartBody.Part =
            firstOrNull { it.name() == name } ?: error("Missing multipart field: $name")

        private fun List<MultipartBody.Part>.optional(name: String): MultipartBody.Part? =
            firstOrNull { it.name() == name }

        private fun MultipartBody.Part.name(): String? =
            headers?.get("Content-Disposition")
                ?.substringAfter("name=\"")
                ?.substringBefore('"')

    }
