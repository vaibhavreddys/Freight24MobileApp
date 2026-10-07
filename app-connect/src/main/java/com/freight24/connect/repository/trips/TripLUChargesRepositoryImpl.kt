package com.freight24.connect.repository.trips

import android.util.Log
import android.webkit.MimeTypeMap
import com.freight24.common.util.DocumentUploadPolicy
import com.freight24.common.di.IoDispatcher
import com.freight24.connect.feature.trips.domain.model.LuChargeRequest
import com.freight24.rest.api.TripLUChargesApi
import com.freight24.rest.response.BaseResponse
import com.freight24.rest.response.SignedDocument
import com.freight24.rest.response.UpdatedLUResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class TripLUChargesRepositoryImpl
    @Inject
    constructor(
        private val api: TripLUChargesApi,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : TripLUChargesRepository {

        override suspend fun updateLUCharges(
            tripId: String,
            loadingFile: File?,
            unloadingFile: File?,
            request: LuChargeRequest,
        ): Result<UpdatedLUResponse> =
            withContext(ioDispatcher) {
                try {
                    val loadingPart = loadingFile?.toMultipart("loadingSlip")
                    val unloadingPart = unloadingFile?.toMultipart("unloadingSlip")

                    val partMap = request.toMultipartMap()

                    val response =
                        api.updateLUCharges(
                            tripId = tripId,
                            loadingFile = loadingPart,
                            unloadingFile = unloadingPart,
                            parts = partMap,
                        )

                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        val errorMsg =
                            try {
                                response.errorBody()?.string()?.let { body ->
                                    Json { ignoreUnknownKeys = true }.decodeFromString<BaseResponse>(body).message
                                }?.takeIf { it.isNotBlank() } ?: response.message()
                            } catch (e: Exception) {
                                response.message()
                            }
                        Result.failure(Exception(errorMsg))
                    }
                } catch (e: Exception) {
                    Log.e("TripLUChargesRepository", "updateLUCharges: ", e)
                    Result.failure(e)
                }
            }

        override suspend fun getSignedLUDocument(
            tripId: String,
            chargeId: String,
        ): Result<SignedDocument> =
            withContext(ioDispatcher) {
                try {
                    val response =
                        api.getSignedLUDocument(
                            tripId = tripId,
                            chargeId = chargeId,
                        )

                    if (response.isSuccessful && response.body() != null) {
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception(response.message()))
                    }
                } catch (e: Exception) {
                    Log.e("TripLUChargesRepository", "getSignedLUDocument: ", e)
                    Result.failure(e)
                }
            }

        private fun File.toMultipart(partName: String): MultipartBody.Part {
            val mimeType =
                if (extension.equals("pdf", ignoreCase = true)) {
                    "application/pdf"
                } else {
                    getMimeType(this) ?: "application/octet-stream"
                }
            DocumentUploadPolicy.validateFile(this)
            val requestBody = asRequestBody(mimeType.toMediaTypeOrNull())

            return MultipartBody.Part.createFormData(
                partName,
                name,
                requestBody,
            )
        }

        private fun getMimeType(file: File): String? {
            val extension = file.extension.lowercase()
            return MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension)
        }

        private fun LuChargeRequest.toMultipartMap(): Map<String, RequestBody> {
            return buildMap {
                fun add(
                    key: String,
                    value: String?,
                ) {
                    value?.let {
                        put(key, it.toRequestBody("text/plain".toMediaType()))
                    }
                }

                add("loading[amount]", loadingAmount?.toString())
                add("loading[payment_method]", loadingPaymentMethod)
                add("loading[reimbursable]", loadingReimbursable?.toString())

                add("unloading[amount]", unloadingAmount?.toString())
                add("unloading[payment_method]", unloadingPaymentMethod)
                add("unloading[reimbursable]", unloadingReimbursable?.toString())

                add("notes", notes)
            }
        }
    }
