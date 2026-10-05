package com.freight24.connect.feature.trips.domain.usecase

import android.webkit.MimeTypeMap
import com.freight24.common.usecase.UseCase
import com.freight24.common.usecase.UseCaseParams
import com.freight24.connect.repository.trips.TripRepository
import com.freight24.rest.response.TripDataResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

data class UploadLrUseCaseDataParams(
    val tripId: String,
    val file: File,
    val notes: String?,
) : UseCaseParams

internal interface UploadLrUseCase : UseCase<UploadLrUseCaseDataParams, TripDataResponse>

class UploadLrUseCaseImpl
    @Inject
    constructor(
        private val repository: TripRepository,
    ) : UploadLrUseCase {
        override suspend fun invoke(params: UploadLrUseCaseDataParams): Result<TripDataResponse> {
            val mimeType =
                if (params.file.extension.equals("pdf", ignoreCase = true)) {
                    "application/pdf"
                } else {
                    getMimeType(params.file.absolutePath) ?: "application/octet-stream"
                }
            val requestFile = params.file.asRequestBody(mimeType.toMediaTypeOrNull())

            val filePart =
                MultipartBody.Part.createFormData(
                    "file", // The name expected by the server for the file field
                    params.file.name, // The actual file name
                    requestFile,
                )

            return repository.uploadLr(
                params.tripId,
                filePart,
                params.notes?.toRequestBody("text/plain".toMediaType()),
            )
        }

        fun getMimeType(url: String): String? {
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
    }
